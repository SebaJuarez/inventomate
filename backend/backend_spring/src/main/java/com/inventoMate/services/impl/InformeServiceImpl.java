package com.inventoMate.services.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.inventoMate.dtos.informes.DecisionRequest;
import com.inventoMate.dtos.informes.DecisionResponse;
import com.inventoMate.dtos.informes.InformeDTO;
import com.inventoMate.entities.Decision;
import com.inventoMate.entities.Empresa;
import com.inventoMate.entities.Informe;
import com.inventoMate.entities.Sucursal;
import com.inventoMate.entities.TipoInforme;
import com.inventoMate.entities.Usuario;
import com.inventoMate.exceptions.ResourceNotFoundException;
import com.inventoMate.mapper.DecisionMapper;
import com.inventoMate.mapper.InformeMapper;
import com.inventoMate.models.EmailSender;
import com.inventoMate.repositories.DecisionRepository;
import com.inventoMate.repositories.InformeRepository;
import com.inventoMate.repositories.UsuarioRepository;
import com.inventoMate.services.FlaskService;
import com.inventoMate.services.InformeService;
import com.inventoMate.services.MlService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class InformeServiceImpl implements InformeService {

	private final UsuarioRepository usuarioRepository;
	private final MlService mlService;
	private final InformeMapper mapper;
	private final FlaskService flaskService;
	private final InformeRepository informeRepository;
	private final DecisionRepository decisionRepository;
	private final DecisionMapper decisionMapper;
	private final EmailSender emailSender;

	@Override
	public void informeDeTendencia(String idAuth0, Long idSucursal) {

		Usuario usuario = usuarioRepository.findByIdAuth0(idAuth0)
				.orElseThrow(() -> new ResourceNotFoundException("Usuario", "id_auth0", idAuth0));

		Empresa empresa = usuario.obtenerEmpresa();

		Sucursal sucursal = empresa.obtenerSucursal(idSucursal);

		if (sucursal == null)
			throw new ResourceNotFoundException("Sucursal", "id_empresa", empresa.getIdEmpresa().toString());

		List<String> productos = empresa.obtenerProductosDeSucursal(sucursal);

		var responseMeli = mlService.getTendencias(productos);

		String idMongo = flaskService.postDatosInformeTendencias(responseMeli);

		Informe informe = mapper.mapToInforme(idMongo, TipoInforme.ANALISIS_DE_TENDENCIA);

		sucursal.agregarInforme(informe);
		sucursal.setEmailSender(emailSender);
		sucursal.generarNotificacionDeInforme(informe);
		informeRepository.save(informe);
	}

	@Override
	public void informeDeProyeccion(String subject, Long idSucursal, LocalDate fechaProyeccion) {

		Usuario usuario = usuarioRepository.findByIdAuth0(subject)
				.orElseThrow(() -> new ResourceNotFoundException("Usuario", "id_auth0", subject));

		Empresa empresa = usuario.obtenerEmpresa();

		Sucursal sucursal = empresa.obtenerSucursal(idSucursal);

		if (sucursal == null)
			throw new ResourceNotFoundException("Sucursal", "id_empresa", empresa.getIdEmpresa().toString());

		var historiaDeCompras = empresa.obtenerHistoricoDeCompras(sucursal);
		var historiaDeVentas = empresa.obtenerHistoricoDeVentas(sucursal);
		String idMongo = flaskService.postDatosInformeProyeccionDeVentas(
				mapper.mapToHistoricoMovimientos(historiaDeVentas, historiaDeCompras, fechaProyeccion, idSucursal));
		Informe informe = mapper.mapToInforme(idMongo, TipoInforme.PROYECCION_DE_VENTAS);
		sucursal.agregarInforme(informe);
		sucursal.setEmailSender(emailSender);
		sucursal.generarNotificacionDeInforme(informe);
		informeRepository.save(informe);
	}
	
	@Override
	public void informeDeSiguientesPedidos(String subject, Long idSucursal) {

		Usuario usuario = usuarioRepository.findByIdAuth0(subject)
				.orElseThrow(() -> new ResourceNotFoundException("Usuario", "id_auth0", subject));

		Empresa empresa = usuario.obtenerEmpresa();

		Sucursal sucursal = empresa.obtenerSucursal(idSucursal);

		if (sucursal == null)
			throw new ResourceNotFoundException("Sucursal", "id_empresa", empresa.getIdEmpresa().toString());

		var historiaDeCompras = empresa.obtenerHistoricoDeCompras(sucursal);
		var historiaDeVentas = empresa.obtenerHistoricoDeVentas(sucursal);
		var productosDeSucursal = empresa.obtenerProductos(sucursal);
		String idMongo = flaskService.postDatosInformeSiguientesPedidos(
				mapper.mapToProductoInformation(historiaDeVentas, historiaDeCompras, productosDeSucursal, idSucursal));
		Informe informe = mapper.mapToInforme(idMongo, TipoInforme.SIGUIENTES_PEDIDOS);
		sucursal.agregarInforme(informe);
		sucursal.setEmailSender(emailSender);
		sucursal.generarNotificacionDeInforme(informe);
		informeRepository.save(informe);
	}
	
	@Override
	public void informeDeObsolescencia(String subject, Long idSucursal) {
		
		Usuario usuario = usuarioRepository.findByIdAuth0(subject)
				.orElseThrow(() -> new ResourceNotFoundException("Usuario", "id_auth0", subject));

		Empresa empresa = usuario.obtenerEmpresa();

		Sucursal sucursal = empresa.obtenerSucursal(idSucursal);

		if (sucursal == null)
			throw new ResourceNotFoundException("Sucursal", "id_empresa", empresa.getIdEmpresa().toString());
		
		var historiaDeVentas = empresa.obtenerHistoricoDeVentas(sucursal);
		var productosDeSucursalObsolescencia = empresa.obtenerProductos(sucursal);
		
		String idMongo = flaskService.postDatosInformeObsolescencia(
				mapper.mapToProductoInformation(historiaDeVentas, productosDeSucursalObsolescencia, idSucursal));
		
		Informe informe = mapper.mapToInforme(idMongo, TipoInforme.OBSOLESCENCIA);
		sucursal.agregarInforme(informe);
		sucursal.setEmailSender(emailSender);
		sucursal.generarNotificacionDeInforme(informe);
		informeRepository.save(informe);
	}

	@Override
	public List<InformeDTO> getInformesByIdSucursalAndTipoInforme(String subject, Long idSucursal,
			TipoInforme tipoInformes) {

		Usuario usuario = usuarioRepository.findByIdAuth0(subject)
				.orElseThrow(() -> new ResourceNotFoundException("Usuario", "id_auth0", subject));

		Empresa empresa = usuario.obtenerEmpresa();

		Sucursal sucursal = empresa.obtenerSucursal(idSucursal);

		if (sucursal == null)
			throw new ResourceNotFoundException("Sucursal", "id_empresa", empresa.getIdEmpresa().toString());

		List<Informe> informes = sucursal.obtenerInformes(tipoInformes);

		return mapper.mapToInformeDTO(informes);
	}

	@Override
	public Object getInformeByIdInformeAndIdSucursal(String subject, Long idSucursal, Long idInforme, TipoInforme tipoInforme) {

		Usuario usuario = usuarioRepository.findByIdAuth0(subject)
				.orElseThrow(() -> new ResourceNotFoundException("Usuario", "id_auth0", subject));

		Empresa empresa = usuario.obtenerEmpresa();

		Sucursal sucursal = empresa.obtenerSucursal(idSucursal);

		if (sucursal == null)
			throw new ResourceNotFoundException("Sucursal", "id_empresa", empresa.getIdEmpresa().toString());

		Informe informe = sucursal.obtenerInforme(idInforme);

		if (informe == null)
			throw new ResourceNotFoundException("Informe", "id_sucursal", sucursal.getIdSucursal().toString());

		if(!informe.getTipoInforme().equals(tipoInforme))
			throw new ResourceNotFoundException("Informe", "tipoInforme", tipoInforme.toString());
		
		informe.setVisto(true);
		informeRepository.save(informe);
		return flaskService.getDatosInformeByTipoInforme(informe.getIdMongo(), informe.getTipoInforme());
	}

	@Override
	public void deleteInformeByIdInformeAndIdSucursal(String subject, Long idSucursal, Long idInforme, TipoInforme tipoInforme) {
		Usuario usuario = usuarioRepository.findByIdAuth0(subject)
				.orElseThrow(() -> new ResourceNotFoundException("Usuario", "id_auth0", subject));

		Empresa empresa = usuario.obtenerEmpresa();

		Sucursal sucursal = empresa.obtenerSucursal(idSucursal);

		if (sucursal == null)
			throw new ResourceNotFoundException("Sucursal", "id_empresa", empresa.getIdEmpresa().toString());

		Informe informe = sucursal.borrarInforme(idInforme);

		if (informe == null)
			throw new ResourceNotFoundException("Informe", "id_sucursal", sucursal.getIdSucursal().toString());
		
		if(!informe.getTipoInforme().equals(tipoInforme))
			throw new ResourceNotFoundException("Informe", "tipoInforme", tipoInforme.toString());
		
		var response = flaskService.deleteInformeByIdAndTipoInforme(informe.getIdMongo(), informe.getTipoInforme());
		
		if(response != HttpStatus.NO_CONTENT) 
			throw new RuntimeException("error al eliminar el informe desde mongo : " + response.toString());
		
		informeRepository.delete(informe);
	}

	@Override
	public void informeDeDecision(String subject, Long idInforme, Long idSucursal, DecisionRequest decisionRequest) {
		
		Usuario usuario = usuarioRepository.findByIdAuth0(subject)
				.orElseThrow(() -> new ResourceNotFoundException("Usuario", "id_auth0", subject));

		Empresa empresa = usuario.obtenerEmpresa();

		Sucursal sucursal = empresa.obtenerSucursal(idSucursal);

		if (sucursal == null)
			throw new ResourceNotFoundException("Sucursal", "id_empresa", empresa.getIdEmpresa().toString());

		Informe informe = sucursal.obtenerInforme(idInforme);

		if (informe == null)
			throw new ResourceNotFoundException("Informe", "id_sucursal", sucursal.getIdSucursal().toString());
		
		Decision decision = decisionMapper.mapToDecision(decisionRequest, informe, usuario);
		
		informe.agregarDecision(decision);
		decisionRepository.save(decision);	
		sucursal.setEmailSender(emailSender);
		sucursal.generarNotificacionDeInforme(informe, usuario);
	}

	@Override
	public List<InformeDTO> getInformesConDecisiones(String subject, Long idSucursal) {

		Usuario usuario = usuarioRepository.findByIdAuth0(subject)
				.orElseThrow(() -> new ResourceNotFoundException("Usuario", "id_auth0", subject));

		Empresa empresa = usuario.obtenerEmpresa();

		Sucursal sucursal = empresa.obtenerSucursal(idSucursal);

		if (sucursal == null)
			throw new ResourceNotFoundException("Sucursal", "id_empresa", empresa.getIdEmpresa().toString());

		List<Informe> informes = sucursal.obtenerInformesConDecisiones();
		
		return mapper.mapToInformeDTO(informes);
	}

	@Override
	public List<DecisionResponse> getDecisionesDelInforme(String subject, Long idSucursal, Long idInforme) {
		
		Usuario usuario = usuarioRepository.findByIdAuth0(subject)
				.orElseThrow(() -> new ResourceNotFoundException("Usuario", "id_auth0", subject));

		Empresa empresa = usuario.obtenerEmpresa();

		Sucursal sucursal = empresa.obtenerSucursal(idSucursal);

		if (sucursal == null)
			throw new ResourceNotFoundException("Sucursal", "id_empresa", empresa.getIdEmpresa().toString());
		
		Informe informe = sucursal.obtenerInforme(idInforme);

		if (informe == null)
			throw new ResourceNotFoundException("Informe", "id_sucursal", sucursal.getIdSucursal().toString());
		
		return informe.getDecisiones().stream()
				.map(decision -> decisionMapper.
						mapToDecisionResponse(usuarioRepository.findById(decision.getIdEmpleado()).orElse(null), decision))
				.collect(Collectors.toList());
	}

	@Override
	public void deleteDecisionDelInforme(String subject, Long idSucursal, Long idInforme, Long idDecision) {
		
		Usuario usuario = usuarioRepository.findByIdAuth0(subject)
				.orElseThrow(() -> new ResourceNotFoundException("Usuario", "id_auth0", subject));

		Empresa empresa = usuario.obtenerEmpresa();

		Sucursal sucursal = empresa.obtenerSucursal(idSucursal);

		if (sucursal == null)
			throw new ResourceNotFoundException("Sucursal", "id_empresa", empresa.getIdEmpresa().toString());
		
		Informe informe = sucursal.obtenerInforme(idInforme);

		if (informe == null)
			throw new ResourceNotFoundException("Informe", "id_sucursal", sucursal.getIdSucursal().toString());
		
		Decision decision = informe.eliminarDecision(idDecision);
		
		if (decision == null)
			throw new ResourceNotFoundException("Decision", "id_informe", informe.getId().toString());
		
		decisionRepository.delete(decision);
	}
}
