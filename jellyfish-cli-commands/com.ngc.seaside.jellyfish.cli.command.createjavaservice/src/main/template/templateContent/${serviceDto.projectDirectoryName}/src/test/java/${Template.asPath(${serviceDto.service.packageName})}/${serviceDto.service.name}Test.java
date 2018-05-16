package ${serviceDto.service.packageName};

import com.ngc.blocs.service.log.api.ILogService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.fail;

@RunWith(MockitoJUnitRunner.class)
public class ${serviceDto.service.name}Test {

   private ${serviceDto.service.name} service;

   @Mock
   private ILogService logService;

   @Before
   public void setup() throws Throwable {
      service = new ${serviceDto.service.name}();
      service.setLogService(logService);
      service.activate();
   }

#foreach ($method in $baseServiceDto.basicPubSubMethods)
   @Test
   public void ${method.name}Test() throws Exception {
      // TODO: implement this
      fail("not implemented");
   }

#end
#foreach ($method in $baseServiceDto.basicServerReqResMethods)
   @Test
   public void ${method.name}Test() throws Exception {
      // TODO: implement this
      fail("not implemented");
}

#end
#foreach ($method in $baseServiceDto.basicSinkMethods)
   @Test
   public void ${method.serviceMethod}Test() throws Exception {
   // TODO: implement this
      fail("not implemented");
   }

#end
#foreach ($method in $baseServiceDto.correlationMethods)
   @Test
   public void ${method.name}Test() throws Exception {
      // TODO: implement this
      fail("not implemented");
   }

#end
#foreach ($method in $baseServiceDto.complexScenarios)
   @Test
   public void ${method.serviceMethod}Test() throws Exception {
      // TODO: implement this
      fail("not implemented");
   }

#end
   @After
   public void cleanup() throws Throwable {
      service.deactivate();
   }
}