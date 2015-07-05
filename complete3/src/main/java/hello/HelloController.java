package hello;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
 
	
	@Value("${rabbit.host}")
	private String rabbitHost;

	@Value("${RabbitMQ.Port}")
	private int rabbitPort;

	@Value("${RabbitMQ.Username}")
	private String rabbitLogin;

	@Value("${RabbitMQ.Password}")
	private String rabbitPassword;	
	
    @Autowired
    CustomerRepository repository;
    
	final static String queueName = "spring-boot";

	//@Autowired
	//AnnotationConfigApplicationContext context;

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Bean
	public ConnectionFactory connectionFactory() {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory(rabbitHost);
		connectionFactory.setPort(rabbitPort);
		connectionFactory.setUsername(rabbitLogin);
		connectionFactory.setPassword(rabbitPassword);
		return connectionFactory;
	}
	
	@Bean
	Queue queue() {
		return new Queue(queueName, false);
	}

	@Bean
	TopicExchange exchange() {
		return new TopicExchange("spring-boot-exchange");
	}

	@Bean
	Binding binding(Queue queue, TopicExchange exchange) {
		return BindingBuilder.bind(queue).to(exchange).with(queueName);
	}

	@Bean
	SimpleMessageListenerContainer container(ConnectionFactory connectionFactory, MessageListenerAdapter listenerAdapter) {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueueNames(queueName);
		container.setMessageListener(listenerAdapter);
		return container;
	}

    @Bean
    Receiver receiver() {
        return new Receiver();
    }

	@Bean
	MessageListenerAdapter listenerAdapter(Receiver receiver) {
		return new MessageListenerAdapter(receiver, "receiveMessage");
	}   
    
	
    @RequestMapping("/")
    public String index()  throws Exception{
      
    	 repository.save(new Customer("Jack", "Bauer"));
         repository.save(new Customer("Chloe", "O'Brian"));
         repository.save(new Customer("Kim", "Bauer"));
         repository.save(new Customer("David", "Palmer"));
         repository.save(new Customer("Michelle", "Dessler"));

         // fetch all customers
         System.out.println("Customers found with findAll():");
         System.out.println("-------------------------------");
         for (Customer customer : repository.findAll()) {
             System.out.println(customer);
         }
         System.out.println();

         // fetch an individual customer by ID
         Customer customer = repository.findOne(1L);
         System.out.println("Customer found with findOne(1L):");
         System.out.println("--------------------------------");
         System.out.println(customer);
         System.out.println();

         // fetch customers by last name
         System.out.println("Customer found with findByLastName('Bauer'):");
         System.out.println("--------------------------------------------");
         for (Customer bauer : repository.findByLastName("Bauer")) {
             System.out.println(bauer);
         }
         
         rabbitTemplate.convertAndSend(queueName, "Hello from RabbitMQ!");
         
    	return "Hello World";
        
    }
    
}
