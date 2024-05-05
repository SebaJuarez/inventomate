package com.inventoMate.services;

import java.util.List;

import com.inventoMate.entities.InvitacionSucursal;

public interface InvitacionService {

	String generarTokenInvitacion(Long idUsuario, Long idSucursal, List<Long> idsRoles);

	InvitacionSucursal getInvitacionByToken(String token);

}