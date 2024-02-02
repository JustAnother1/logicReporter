package de.nomagic.logic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SampleSource
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    protected boolean valid;

    private boolean activeSegment_isHigh;
    private double activeSegment_endTime;

    protected SampleSource()
    {

    }

    public boolean isValid()
    {
        return valid;
    }

    public abstract long getNumberEdges();
    protected abstract double getNextEndTime();

    protected void initializeSampleQueue(boolean initiallyHigh)
    {
        activeSegment_endTime = getNextEndTime();
        activeSegment_isHigh = initiallyHigh;
    }

    public double getTimeOfEdgeAfter(double edgeTime)
    {
        findEdge(edgeTime);
        return activeSegment_endTime;
    }

    public boolean isHighAt(double searchedTime)
    {
        findEdge(searchedTime);
        return activeSegment_isHigh;
    }

    protected void findEdge(double beforeTime)
    {
        while(activeSegment_endTime <= beforeTime)
        {
            activeSegment_endTime = getNextEndTime();
            activeSegment_isHigh = !activeSegment_isHigh;
        }
    }
}
