import dragonBones.factories.NativeFactory;
import dragonBones.utils.InputStreamUtils;
import flash.utils.ByteArray;
import org.junit.Test;

public class ExampleTest {
	@Test
	public void testName() throws Exception {
		NativeFactory factory = new NativeFactory();
		byte[] bytes = InputStreamUtils.readAllBytes(ExampleTest.class.getResourceAsStream("DragonWithClothes.png"));
		factory.parseData(new ByteArray(bytes));
	}
}
