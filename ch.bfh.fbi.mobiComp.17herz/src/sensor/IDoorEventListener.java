package sensor;

/**
 * Copyright 2014 blastbeat syndicate gmbh
 * Author: Roger Jaggi <roger.jaggi@blastbeatsyndicate.com>
 * Date: 07.03.14
 * Time: 11:20
 */
public interface IDoorEventListener {

    public void doorEventHappend(BarometerApplication source, Integer airPressure);
}
