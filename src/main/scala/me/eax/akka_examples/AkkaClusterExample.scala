package me.eax.akka_examples

import akka.cluster.ClusterEvent._
import akka.actor._
import akka.event._
import akka.cluster._

class ClusterListener extends Actor with ActorLogging {

  val cluster = Cluster(context.system)

  // subscribe to cluster changes, re-subscribe when restart
  override def preStart() {
    cluster.subscribe(self, InitialStateAsEvents, classOf[MemberEvent], classOf[UnreachableMember])
  }

  override def postStop() {
    cluster.unsubscribe(self)
  }

  def receive = LoggingReceive {
    case MemberUp(member) =>
      log.info(s"[Listener] node is up: $member")

    case UnreachableMember(member) =>
      log.info(s"[Listener] node is unreachable: $member")

    case MemberRemoved(member, previousStatus) =>
      log.info(s"[Listener] node is removed: $member after $previousStatus")

    case _: MemberEvent => // do nothing
  }
}

object AkkaClusterExample extends App {
  val system = ActorSystem("system")
  system.actorOf(Props[ClusterListener], "clusterListener")
  system.awaitTermination()
}
