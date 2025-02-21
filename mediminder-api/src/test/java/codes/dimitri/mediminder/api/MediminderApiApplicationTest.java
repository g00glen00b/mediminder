package codes.dimitri.mediminder.api;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class MediminderApiApplicationTest {
    @Test
    void contextLoads() {
        ApplicationModules modules = ApplicationModules.of(MediminderApiApplication.class);
        modules.verify();
        Documenter documenter = new Documenter(modules);
        documenter.writeModulesAsPlantUml();
    }
}