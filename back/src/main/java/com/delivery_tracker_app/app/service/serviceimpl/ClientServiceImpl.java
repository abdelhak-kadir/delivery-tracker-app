package com.delivery_tracker_app.app.service.serviceimpl;


import com.delivery_tracker_app.app.dto.v1.client.ClientResponse;
import com.delivery_tracker_app.app.dto.v1.client.CreateClientRequest;
import com.delivery_tracker_app.app.dto.v1.client.UpdateClientRequest;
import com.delivery_tracker_app.app.dto.v1.common.PagedResponse;
import com.delivery_tracker_app.app.entity.Client;
import com.delivery_tracker_app.app.exception.ResourceNotFoundException;
import com.delivery_tracker_app.app.mapper.ClientMapper;
import com.delivery_tracker_app.app.repository.ClientRepo;
import com.delivery_tracker_app.app.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepo clientRepo;
    private final ClientMapper clientMapper;

    @Override
    public ClientResponse create(CreateClientRequest request){
        Client saved = clientRepo.save(clientMapper.toEntity(request));
        return clientMapper.toDto(saved);
    }

    @Override
    public  ClientResponse update(UpdateClientRequest request){

        Client updated = clientRepo.findById(request.id()).orElseThrow(
                () -> new ResourceNotFoundException("Client not found !")
        );
        updated.setName(request.name());
        updated.setIdentityId(request.identityId());

        updated =  clientRepo.save(updated);
        return clientMapper.toDto(updated);
    }

    @Override
    public PagedResponse<ClientResponse> getAll(int page, int size){

        Pageable pageable = PageRequest.of(page, size);
        Page<Client> pageResult = clientRepo.findAll(pageable);
        List<ClientResponse> data = pageResult.getContent().stream().map(clientMapper::toDto).toList();

        return new PagedResponse<>(
                data,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isLast()
        );
    }

    @Override
    public ClientResponse getById(Long id){

        Client response = clientRepo.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Client not found !")
        );
        return clientMapper.toDto(response);
    }

    @Override
    public void delete(Long id){
        if(!clientRepo.existsById(id)){
            throw new ResourceNotFoundException("Client not found !");
        }

        clientRepo.deleteById(id);
    }
}
