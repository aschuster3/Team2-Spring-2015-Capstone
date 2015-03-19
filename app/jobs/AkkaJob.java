package jobs;

import play.libs.Akka;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

/**
 * Created by max on 3/18/15.
 */
public abstract class AkkaJob implements Runnable {

    private FiniteDuration frequency;

    public AkkaJob(FiniteDuration frequency) {
        this.frequency = frequency;
    }

    public void schedule(FiniteDuration initialDelay) {
        Akka.system().scheduler().schedule(
                initialDelay,
                frequency,
                this,
                Akka.system().dispatcher()
        );
    }
}
