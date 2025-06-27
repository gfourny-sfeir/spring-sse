package fr.example.spring.sse.integrationtest;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import fr.example.spring.sse.SpringSSE;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = { SpringSSE.class })
@ActiveProfiles(profiles = { "test" })
public abstract class AbstractITSpring {

}
