package com.smartqueue.inventory_service.repository;

import com.smartqueue.inventory_service.entity.Resource;
import com.smartqueue.inventory_service.entity.ResourceStatus;
import com.smartqueue.inventory_service.entity.ResourceType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ResourceRepositoryTest {

    @Autowired
    private ResourceRepository resourceRepository;

    @Test
    void saveAndFindByName_ShouldReturnResource() {
        Resource resource = Resource.builder()
                .name("Seat A101")
                .type(ResourceType.SEAT)
                .totalQuantity(1)
                .availableQuantity(1)
                .status(ResourceStatus.AVAILABLE)
                .build();

        Resource saved = resourceRepository.save(resource);
        assertThat(saved.getId()).isNotNull();

        Optional<Resource> found = resourceRepository.findByName("Seat A101");
        assertThat(found).isPresent();
        assertThat(found.get().getType()).isEqualTo(ResourceType.SEAT);
    }
}
