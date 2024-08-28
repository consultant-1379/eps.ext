package com.ericsson.component.aia.services.exteps.policy.model;

import static com.ericsson.component.aia.services.exteps.policy.model.Constants.SINGLE_THREADED;

public class ConsumerPolicy extends Policy{

    private int numberOfConsumerThreads = SINGLE_THREADED;

    public int getNumberOfConsumerThreads() {
        return numberOfConsumerThreads;
    }

    public void setNumberOfConsumerThreads(int numberOfConsumerThreads) {
        this.numberOfConsumerThreads = numberOfConsumerThreads;
    }


}
