package com.depli.controller;

import com.depli.service.store.descriptor.MemoryDescriptorService;
import com.depli.store.cache.descriptor.MemoryDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MemoryDescriptorController
 *
 * REST controller to expose memory descriptor data.
 *
 * @author lpsandaruwan
 * @since 3/25/17
 */

@RestController
@RequestMapping("/descriptors/memories")
public class MemoryDescriptorController {

  @Autowired
  private MemoryDescriptorService memoryDescriptorService;

  @GetMapping("/{descriptorIndex}/dynamics")
  public ResponseEntity<MemoryDescriptor> findMemoryDescriptorByNodeId(
      @PathVariable Long descriptorIndex) {
    return new ResponseEntity<>(memoryDescriptorService.getByNodeId(descriptorIndex),
        HttpStatus.OK);
  }
}
