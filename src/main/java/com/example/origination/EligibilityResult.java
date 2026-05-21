package com.example.origination;

import java.util.List;

public class EligibilityResult {
    private boolean eligible;
    private List<String> failureReasons;

    public boolean isEligible()                      { return eligible; }
    public void setEligible(boolean v)               { eligible = v; }
    public List<String> getFailureReasons()          { return failureReasons; }
    public void setFailureReasons(List<String> v)    { failureReasons = v; }
}