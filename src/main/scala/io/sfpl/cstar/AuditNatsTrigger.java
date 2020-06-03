package io.sfpl.cstar;

import org.apache.cassandra.db.Mutation;
import org.apache.cassandra.db.partitions.Partition;
import org.apache.cassandra.db.rows.Row;
import org.apache.cassandra.db.rows.Cell;
import org.apache.cassandra.db.rows.Unfiltered;
import org.apache.cassandra.db.Clustering;
import org.apache.cassandra.db.rows.UnfilteredRowIterator;
import org.apache.cassandra.triggers.ITrigger;

//import org.apache.cassandra.schema.ColumnMetadata; //Cassandra 4
import org.apache.cassandra.config.ColumnDefinition; //Replace with Cass 4

import io.nats.client.AuthHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.Iterator;

import org.json.JSONObject;

public class AuditNatsTrigger implements ITrigger {

    private ThreadPoolExecutor threadPoolExecutor;

    public AuditNatsTrigger() {
        threadPoolExecutor = new ThreadPoolExecutor(4, 20, 30,
                TimeUnit.SECONDS, new LinkedBlockingDeque<>());
    }

    @Override
    public Collection<Mutation> augment(Partition partition) {
        threadPoolExecutor.execute(() -> handleUpdate(partition));
        return Collections.emptyList();
    }

    private void handleUpdate(Partition partition) {
        JSONObject obj = new JSONObject();

        String tableName = partition.metadata().cfName;
        obj.put("__table", tableName); 
        obj.put("__key", partition.metadata().getKeyValidator().getString(partition.partitionKey().getKey()));

        if (partition.partitionLevelDeletion().isLive()) {
            UnfilteredRowIterator it = partition.unfilteredIterator();
            while (it.hasNext()) {
                Unfiltered un = it.next();
                switch (un.kind()) {
                    case ROW:
                        // row
                        Row row = (Row) un;
                        if (row.primaryKeyLivenessInfo().timestamp() != Long.MIN_VALUE) {
                            // only INSERT operation updates row timestamp (LivenessInfo).
                            // For other operations this timestamp is not updated and equals Long.MIN_VALUE
                            obj.put("__action", "insert");
                        } else {
                            if (row.deletion().isLive()) {
                                // row update
                                obj.put("__action", "update");
                            }
                        }
                        break;
                    case RANGE_TOMBSTONE_MARKER:
                        // range deletion
                        break;
                }
                Clustering clt = (Clustering) un.clustering();  
                Iterator<Cell> cells = partition.getRow(clt).cells().iterator();
                Iterator<ColumnDefinition> columns = partition.getRow(clt).columns().iterator();

                while(columns.hasNext()){
                    ColumnDefinition columnDef = columns.next();
                    Cell cell = cells.next();
                    String data = new String(cell.value().array()); // If cell type is text
                    obj.put(columnDef.toString(), data);
                }
            }
        } else {
            // partition level deletion
            obj.put("__action", "delete");
        }
        NatsInterop.Publish("tic.log.audit", new Log("generic", 30, "tic.log.audit", "audit", obj.toString(), null, null, null, null));
    }


}
