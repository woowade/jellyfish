package ${package};

import com.google.inject.Inject;
import com.ngc.blocs.service.log.api.ILogService;
import com.ngc.seaside.jellyfish.api.IUsage;
import com.ngc.seaside.jellyfish.api.IJellyFishCommand;
import com.ngc.seaside.jellyfish.api.IJellyFishCommandOptions;
import com.ngc.seaside.jellyfish.service.buildmgmt.api.IBuildManagementService;
import com.ngc.seaside.jellyfish.service.name.api.IPackageNamingService;
import com.ngc.seaside.jellyfish.service.name.api.IProjectNamingService;
import com.ngc.seaside.jellyfish.service.template.api.ITemplateService;

public class ${classname}GuiceWrapper implements IJellyFishCommand {

   private final ${classname} delegate = new ${classname}();

   @Inject
   public ${classname}GuiceWrapper(ILogService logService,
                                   IBuildManagementService buildManagementService,
                                   ITemplateService templateService,
                                   IPackageNamingService packageNamingService,
                                   IProjectNamingService projectNamingService) {
      delegate.setLogService(logService);
      delegate.setBuildManagementService(buildManagementService);
      delegate.setTemplateService(templateService);
      delegate.setProjectNamingService(projectNamingService);
      delegate.setPackageNamingService(packageNamingService);
   }

   @Override
   public String getName() {
      return delegate.getName();
   }

   @Override
   public IUsage getUsage() {
      return delegate.getUsage();
   }

   @Override
   public void run(IJellyFishCommandOptions commandOptions) {
      delegate.run(commandOptions);
   }

}
