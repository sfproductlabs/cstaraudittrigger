
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


object NatsInterop extends NatsConf {
    var serverUrls = Array("nats://localhost:4222")

    var context = SSLContext.getInstance("TLS"); //SSL,TLS

    //Client Keys
	var clientKeystore = KeyStore.getInstance("PKCS12"); //jks,PKCS12
    var keystorePath = "/home/a/projects/sfpl/cstaraudittrigger/.setup/keys/nats-keystore.jks"
    var keystorePassword = "password"
	var keystoreFis = new FileInputStream(keystorePath);
	clientKeystore.load(keystoreFis, keystorePassword.toCharArray());
	var kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()); //"SunX509", KeyManagerFactory.getDefaultAlgorithm()
	kmf.init(clientKeystore, keystorePassword.toCharArray());

    //Server Keys
	var trustKeystore = KeyStore.getInstance("PKCS12");
    var trustKeystorePath = "/home/a/projects/sfpl/cstaraudittrigger/.setup/keys/nats-truststore.jks"
    var keystoreTrustPassword = "password"
	var trustKeystoreFis = new FileInputStream(trustKeystorePath);
	trustKeystore.load(trustKeystoreFis, keystoreTrustPassword.toCharArray());
	var tmf = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
	tmf.init(trustKeystore);
	
    //SSL Context
	context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
	//sslSocket = (SSLSocket)context.getSocketFactory().createSocket(host, port);

    //Cleanup
	trustKeystoreFis.close();
	keystoreFis.close();
	
	println("NatsInterop TLS initialized.");
    var o = new Options.Builder().servers(serverUrls).sslContext(context).maxReconnects(-1).build()


    var nc = Nats.connect(o)
    nc.publish("subject", "hello world".getBytes(StandardCharsets.UTF_8));
    def Publish( ) : Unit = {
        nc.publish("subject", "hello world".getBytes(StandardCharsets.UTF_8));
    }
}

