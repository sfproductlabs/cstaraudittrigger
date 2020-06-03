
package io.sfpl.cstar

import java.util.Properties

import io.nats.client._

import javax.net.ssl._
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.Socket;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.nio.charset.StandardCharsets

import scala.collection.mutable._
import net.liftweb.json._
import net.liftweb.json.Serialization.write

case class Log(name: String,
                topic: String,
                level: Int
                // ltimenss: String(ns), //ltime nanosecond string
                // ldate: now.match(/(.*)T/i)[1],
                // msg: parsed.msg || null,
                // hostname : hostname,
                // host: hostip,
                // ip : ip || parsed.ip || null,
                // params : parsed.params,
                // owner: parsed.owner
)


object NatsInterop extends NatsConf {
    val serverUrls = Array("nats://localhost:4222")

    val context = SSLContext.getInstance("TLS"); //SSL,TLS

    //Client Keys
	val clientKeystore = KeyStore.getInstance("PKCS12"); //jks,PKCS12
    val keystorePath = "/home/a/projects/sfpl/cstaraudittrigger/.setup/keys/nats-keystore.jks"
    val keystorePassword = "password"
	val keystoreFis = new FileInputStream(keystorePath);
	clientKeystore.load(keystoreFis, keystorePassword.toCharArray());
	val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()); //"SunX509", KeyManagerFactory.getDefaultAlgorithm()
	kmf.init(clientKeystore, keystorePassword.toCharArray());

    //Server Keys
	val trustKeystore = KeyStore.getInstance("PKCS12");
    val trustKeystorePath = "/home/a/projects/sfpl/cstaraudittrigger/.setup/keys/nats-truststore.jks"
    val keystoreTrustPassword = "password"
	val trustKeystoreFis = new FileInputStream(trustKeystorePath);
	trustKeystore.load(trustKeystoreFis, keystoreTrustPassword.toCharArray());
	val tmf = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
	tmf.init(trustKeystore);
	
    //SSL Context
	context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
	//sslSocket = (SSLSocket)context.getSocketFactory().createSocket(host, port);

    //Cleanup
	trustKeystoreFis.close();
	keystoreFis.close();
	
	println("NatsInterop TLS initialized.");
    val o = new Options.Builder().servers(serverUrls).sslContext(context).maxReconnects(-1).build()


    val nc = Nats.connect(o)
    def Publish( ) : Unit = {
        implicit val formats = DefaultFormats
        val l = Log("Woohoo", "generic", 30)
        val jsonString = write(l)
        nc.publish("tic.log.audit", jsonString.getBytes(StandardCharsets.UTF_8));
    }
}

