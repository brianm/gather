package org.skife.gather;

import org.junit.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class CandidateTest
{
    @Test(expected = IllegalArgumentException.class)
    public void testWantIntegerButMethodReturnsString() throws Exception
    {
        Object target = new Object()
        {
            @Priority(1)
            public String foo()
            {
                return "woof";
            }
        };

        new Candidate(Integer.class, target, target.getClass().getMethod("foo"));
    }

    @Test
    public void testMethodWithNoParamsIsSatisfied() throws Exception
    {
        Object target = new Object()
        {
            @Priority(1)
            public String foo()
            {
                return "foo";
            }
        };

        Candidate c = new Candidate(String.class, target, target.getClass().getMethod("foo"));

        assertThat(c.isSatisfiedBy(Collections.emptySet()))
            .describedAs("candidate.isSatisfiedBy")
            .isTrue();

    }


}
