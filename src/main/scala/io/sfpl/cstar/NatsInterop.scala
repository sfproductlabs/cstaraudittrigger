
package io.sfpl.cstar

import java.util.Properties
import io.nats._

object NatsInterop {

def printMe( ) : Unit = {
      println("Hello, Trigger")
}
}

// var opts : Properties = new Properties
// opts.put("queue", "job.workers");
// var conn = Conn.connect(opts)

// conn.subscribe("help", (msg:Msg) => {
//   println("Msg received only once through the 'job.workers' queue: " + msg.body)})
