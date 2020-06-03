
package io.sfpl.cstar

import java.util.Properties

import io.nats.client._

import javax.net.ssl._
import javax.net.ssl.KeyManager
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509KeyManager
import javax.net.ssl.X509TrustManager
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.net.Socket
import java.security.KeyStore
import java.security.Principal
import java.security.PrivateKey
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.nio.charset.StandardCharsets

import scala.collection.mutable._

import java.time._
import java.util._
import java.time.format.DateTimeFormatter;

import org.json.JSONObject;


object NatsClock {
  val clock = new NanoClock()
  val start = Instant.now(clock)
}

@SerialVersionUID(100L)
class Log() extends Serializable {
    var topic : String = null
    var level : Int = 0
    var name: String = null;
    var msg: String = null;
    var params: String = null;
    var hostname: String = null;
    var host: String = null;
    var ip: String = null;
    var owner: String = null;
    private val _i : Instant = Instant.now(NatsClock.clock)
    private val _datetime: LocalDateTime = LocalDateTime.ofInstant(_i, ZoneOffset.UTC)
    val ldate = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(_datetime)
    val ltimens: Long = _datetime.getHour().asInstanceOf[Long]*3600*1000000000 + _datetime.getMinute().asInstanceOf[Long]*60*1000000000 + _datetime.getSecond().asInstanceOf[Long]*1000000000 + (_i.getNano().asInstanceOf[Long] % 1000000000)        
    val ltimenss: String = ltimens.toString;

    def this(
        topic: String, 
        level: Int, 
        name: String = null,
        msg: String = null,
        params : String = null,
        hostname : String = null,
        host: String = null,
        ip : String = null,
        owner: String = null
    )
    {                 
        this() 
        this.topic=topic
        this.level=level
        this.name=name 
        this.msg = msg
        this.params = params
        this.hostname = hostname
        this.host = host
        this.ip = ip
        this.owner = owner
    } 

    override def toString() : String = { 
        var obj = new JSONObject();
        obj.put("topic", topic);
        obj.put("level", level);
        obj.put("name", name);
        obj.put("msg", msg);
        obj.put("params", params);
        obj.put("hostname", hostname);
        obj.put("host", host);
        obj.put("ip", ip);
        obj.put("owner", owner);
        obj.put("ldate", ldate);
        obj.put("ltimenss", ltimenss);
        return obj.toString()
    } 
    
}


object NatsInterop extends NatsConf {
    val serverUrls = natsServers.split(",")

    val context = SSLContext.getInstance("TLS"); //SSL,TLS

    //Client Keys
	val clientKeystore = KeyStore.getInstance("PKCS12"); //jks,PKCS12
	val keystoreFis = new FileInputStream(natsKeystorePath);
	clientKeystore.load(keystoreFis, natsKeystorePassword.toCharArray());
	val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()); //"SunX509", KeyManagerFactory.getDefaultAlgorithm()
	kmf.init(clientKeystore, natsKeystorePassword.toCharArray());

    //Server Keys
	val trustKeystore = KeyStore.getInstance("PKCS12");
	val trustKeystoreFis = new FileInputStream(natsTruststorePath);
	trustKeystore.load(trustKeystoreFis, natsTruststorePassword.toCharArray());
	val tmf = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
	tmf.init(trustKeystore);
	
    //SSL Context
	context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
	//sslSocket = (SSLSocket)context.getSocketFactory().createSocket(host, port);

    //Cleanup
	trustKeystoreFis.close();
	keystoreFis.close(); 
	
	println("[INFO] NatsInterop TLS Trigger Initialized.");
    val o = new Options.Builder().servers(serverUrls).sslContext(context).maxReconnects(-1).build()


    val nc = Nats.connect(o)
    def Publish(l:Log) : Unit = {       
        Publish(l.name, l);
    }

    def Publish(channel: String, l:Log) : Unit = {        
        var sendTo = channel
        if (sendTo == null || sendTo == "") {
            sendTo = natsDefaultTopic
        }
        nc.publish(channel, l.toString().getBytes(StandardCharsets.UTF_8));
    }
}

