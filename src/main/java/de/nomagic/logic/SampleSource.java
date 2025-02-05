package de.nomagic.logic;

public abstract class SampleSource
{
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

    protected abstract double getNextEndTime();

    protected void initializeSampleQueue(boolean initiallyHigh)
    {
        activeSegment_endTime = getNextEndTime();
        activeSegment_isHigh = initiallyHigh;
    }

    protected void findEdge(double beforeTime)
    {
        double lastEndTime = 0.0;
        while(activeSegment_endTime <= beforeTime)
        {
            activeSegment_endTime = getNextEndTime();
            if(lastEndTime != activeSegment_endTime)
            {
                activeSegment_isHigh = !activeSegment_isHigh;
                lastEndTime = activeSegment_endTime;
            }
            else
            {
                // reached end
                return;
            }
        }
    }
}
