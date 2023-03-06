package my.love.mother.AppWebFlux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import reactor.blockhound.BlockHound;

@SpringBootApplication
@ComponentScan(basePackages = "my.love.mother.AppWebFlux")
public class AppWebFluxApplication {
	static {
		BlockHound.install(
				builder -> builder
						.allowBlockingCallsInside("java.util.UUID", "randomUUID")
						.allowBlockingCallsInside("java.io.FilterInputStream", "read")
						.allowBlockingCallsInside("java.io.InputStream", "readNBytes")
		);
	}

	public static void main(String[] args) {
		//http://localhost:1221/webjars/swagger-ui/index.html
		SpringApplication.run(AppWebFluxApplication.class, args);
	}
}
