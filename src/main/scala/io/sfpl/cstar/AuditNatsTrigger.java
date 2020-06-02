package io.sfpl.cstar;

import org.apache.cassandra.db.Mutation;
import org.apache.cassandra.db.partitions.Partition;
import org.apache.cassandra.db.rows.Row;
import org.apache.cassandra.db.rows.Unfiltered;
import org.apache.cassandra.db.rows.UnfilteredRowIterator;
import org.apache.cassandra.triggers.ITrigger;


import io.nats.client.AuthHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AuditNatsTrigger implements ITrigger {

    //private String siddiTopic;
    //private Producer<String, String> producer;
    private ThreadPoolExecutor threadPoolExecutor;

    public AuditNatsTrigger() {
        
        NatsInterop.printMe();
        //siddiTopic = getEnv("NATS_TOPIC");
        // producer = new NatsProducer<>(getProps());
        threadPoolExecutor = new ThreadPoolExecutor(4, 20, 30,
                TimeUnit.SECONDS, new LinkedBlockingDeque<>());
    }

    @Override
    public Collection<Mutation> augment(Partition partition) {
        threadPoolExecutor.execute(() -> handleUpdate(partition));
        return Collections.emptyList();
    }

    private void handleUpdate(Partition partition) {
        if (partition.partitionLevelDeletion().isLive()) {
            UnfilteredRowIterator it = partition.unfilteredIterator();
            while (it.hasNext()) {
                Unfiltered un = it.next();
                switch (un.kind()) {
                    case ROW:
                        // row
                        Row row = (Row) un;
                        if (row.primaryKeyLivenessInfo().timestamp() != Long.MIN_VALUE) {
                            // row insert
                            // only INSERT operation updates row timestamp (LivenessInfo).
                            // For other operations this timestamp is not updated and equals Long.MIN_VALUE
                            System.out.println("row insert");

                            // produce insert
                            // ProducerRecord<String, String> record = new ProducerRecord<>(siddiTopic, "INSERT");
                            // producer.send(record);
                        } else {
                            if (row.deletion().isLive()) {
                                // row update
                                System.out.println("row update");

                                // produce update
                                // ProducerRecord<String, String> record = new ProducerRecord<>(siddiTopic, "UPDATE");
                                // producer.send(record);
                            }
                        }
                        break;
                    case RANGE_TOMBSTONE_MARKER:
                        // range deletion
                        break;
                }
            }
        } else {
            // partition level deletion
            System.out.println("partition delete");

            // produce delete
            // ProducerRecord<String, String> record = new ProducerRecord<>(siddiTopic, "DELETE");
            // producer.send(record);
        }
    }

    private Properties getProps() {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", getEnv("NATS_ADDR"));
        properties.put("key.serializer", "org.apache.nats.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.nats.common.serialization.StringSerializer");

        return properties;
    }

    private String getEnv(String name) {
        String env = System.getenv(name);
        System.out.println("read env " + name + " - " + env);

        if (env == null) return "";
        else return env;
    }

}
