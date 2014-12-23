import org.junit.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class StuffTest
{
    @Test
    public void testDuration() throws Exception
    {
        Duration d = Duration.ofSeconds(1);
        assertThat(d.toMillis()).isEqualTo(1000);
    }
}
