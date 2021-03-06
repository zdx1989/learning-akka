package com.akkademy

import akka.actor.{ActorSystem, Props}
import akka.routing.BalancingPool
import com.akkademy.ArticleParseActor.ParseArticle
import com.akkademy.TestHelper.TestCameoActor
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.{Await, Promise}
import scala.concurrent.duration._

/**
  * Created by zhoudunxiong on 2018/9/10.
  */
class BalancingPoolSpec extends FlatSpec with Matchers {
  val system = ActorSystem()

  "balancingPool" should "do work concurrently" in {
    val p = Promise[String]()
    val workRoute = system.actorOf(
      BalancingPool(8).props(Props[ArticleParseActor]),
      "Balancing-pool-router"
    )

    val cameoActor = system.actorOf(Props(new TestCameoActor(p)))

    (0 to 2000).foreach { _ =>
      workRoute.tell(ParseArticle(TestHelper.file), cameoActor)
    }
    TestHelper.profile(() => Await.ready(p.future, 20.seconds), "ActorInBalancingPool")
  }
}
