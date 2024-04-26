package com.inventoMate.services.impl;

import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.users.User;
import com.inventoMate.dtos.empresas.EmpresaDTO;
import com.inventoMate.dtos.roles.RolDTO;
import com.inventoMate.dtos.sucursales.SucursalDTO;
import com.inventoMate.dtos.users.UsuarioDTO;
import com.inventoMate.entities.Empresa;
import com.inventoMate.entities.Sucursal;
import com.inventoMate.entities.Usuario;
import com.inventoMate.exceptions.ResourceAlreadyExistsException;
import com.inventoMate.exceptions.ResourceNotFoundException;
import com.inventoMate.payload.EditUserRequest;
import com.inventoMate.repositories.EmpresaRepository;
import com.inventoMate.repositories.SucursalRepository;
import com.inventoMate.repositories.UsuarioRepository;
import com.inventoMate.services.UserAuth0Service;
import com.inventoMate.services.UsuarioService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@AllArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class UsuarioServiceImpl implements UsuarioService {

	private final UsuarioRepository usuarioRepository;
	private final EmpresaRepository empresaRepository;
	private final UserAuth0Service userAuthService;
	private final SucursalRepository sucursalRepository;
	private final ModelMapper modelMapper;
	
	public UsuarioDTO createUsuario(String idAuth0) throws Auth0Exception {
		
		// obtengo el usuario de auth0 por su Id
		User userAuth0 = userAuthService.getUserById(idAuth0);
		
		
		// valido que no exista un usuario ya creado
		if(usuarioRepository.existsByIdAuth0(idAuth0)) {
			throw new ResourceAlreadyExistsException("Usuario","Id_Auth0", idAuth0);
		}
		
		// creo el usuario con los atributos requeridos
		Usuario usuario = new Usuario();
		
		usuario.setEmail(userAuth0.getEmail());
		usuario.setIdAuth0(userAuth0.getId());
		usuario.setNickname(userAuth0.getNickname());
		usuario.setPicture(userAuth0.getPicture());
		
		// lo guardo en la bd
		usuarioRepository.save(usuario);
		
		// mapeo el usuario en su DTO
		return modelMapper.map(usuario, UsuarioDTO.class);
	}

	@Override
	public UsuarioDTO getProfileCurrentUser(String idAuth0) throws Auth0Exception {
		
		// recupero usuario principal
		Usuario usuario  = userByAuth0Id(idAuth0);
		// recupero empresa si es dueño
		Empresa empresa = null;
		if(empresaRepository.existsByOwner(usuario))
			empresaRepository.findByOwner(usuario).orElse(null);
		// recupero sucursal si es empleado
		Sucursal sucursal = null;
		if(sucursalRepository.existsByUsuariosContains(usuario))
			sucursal = sucursalRepository.findByUsuariosContains(usuario).orElse(null);
		// recupero empresa para la que trabaja si es empleado
		if(sucursal != null)
			empresa = empresaRepository.findBySucursalesContains(sucursal).orElse(null);
		// mapeo entidades a dtos
		EmpresaDTO empresaResponse= empresa != null? modelMapper.map(empresa, EmpresaDTO.class) : null;
		SucursalDTO sucursalResponse= sucursal != null? modelMapper.map(sucursal, SucursalDTO.class) : null;
		UsuarioDTO usuarioProfile = UsuarioDTO.builder().email(usuario.getEmail())
				.empresa(empresaResponse)
				.sucursal(sucursalResponse)
				.idUsuario(usuario.getIdUsuario())
				.nickname(usuario.getNickname())
				.picture(usuario.getPicture())
				.roles(usuario.getRoles().stream()
						.map(rol -> modelMapper
								.map(rol, RolDTO.class))
						.collect(Collectors.toList()))
				.build();
		
		return usuarioProfile;
	}

	@Override
	public UsuarioDTO updateUser(String id, @Valid EditUserRequest usuarioEditRequest) throws Auth0Exception{
		
		// obtengo usuario por auth0 id
		Usuario usuario = userByAuth0Id(id);
		
		//actualizo campos en BD local
		if(usuarioEditRequest.getEmail() != null) usuario.setEmail(usuarioEditRequest.getEmail());
		usuario.setNickname(usuarioEditRequest.getNickname());
		usuario.setPicture(usuarioEditRequest.getPicture());
		usuarioRepository.save(usuario);			
		//actualizo campos en BD auth0
		userAuthService.updateUser(id, usuarioEditRequest);
		
		return getProfileCurrentUser(id);
	}
	
	private Usuario userByAuth0Id(String idAuth0) {
		return usuarioRepository.findByIdAuth0(idAuth0).orElseThrow(()-> 
			new ResourceNotFoundException("Usuario", "id_auth0", idAuth0)
		);
	}

	@Override
	@Transactional
	public void deleteUserPrincipal(String idAuth0){
		usuarioRepository.deleteByIdAuth0(idAuth0);
	}
}
