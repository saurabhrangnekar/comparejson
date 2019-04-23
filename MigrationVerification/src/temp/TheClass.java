package temp;

@ConsumerClass
public class TheClass{
	@MessageConsumer(types= {"topic1","topic2"})
	public void doSomething(String message){
		System.out.println("Received message : " + message);
  	}
}