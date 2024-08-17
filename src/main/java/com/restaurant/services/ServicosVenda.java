package com.restaurant.services;

import com.restaurant.dtos.CriarVendaDTO;
import com.restaurant.dtos.DetalheVendaDTO;
import com.restaurant.dtos.mapper.VendaMapper;
import com.restaurant.models.ProdutosEntity;
import com.restaurant.models.VendaEntity;
import com.restaurant.repositories.ProdutosRepository;
import com.restaurant.repositories.VendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ServicosVenda {

    @Autowired
    private VendaRepository vendaRepository;

    @Autowired
    private ProdutosRepository produtoRepository;

    @Autowired
    private VendaMapper vendaMapper;

    public DetalheVendaDTO criar(CriarVendaDTO vendaDTO) {
        try {
            VendaEntity vendaEntity = vendaMapper.criarVendaDtoParaVendaEntity(vendaDTO);

            if (vendaEntity.getProdutos().isEmpty()) throw new RuntimeException("Nenhum produto selecionado");

            List<ProdutosEntity> produtosLista = new ArrayList<>();
            vendaDTO.produtosEntities().forEach(produto -> {
                produto = produtoRepository.findById(produto.getId()).orElseThrow(() -> new RuntimeException("Produto não encontrado"));
                produtosLista.add(produto);
            });

            vendaEntity.setProdutos(produtosLista);
            vendaEntity.setSomaTotalDaListaDeProdutos(somaProdutosVenda(vendaDTO.produtosEntities()));
            vendaEntity.setDataDaVenda(LocalDateTime.now());

            return vendaMapper.vendaEntityParaDetalheVendaDto(vendaRepository.save(vendaEntity));
        } catch (RuntimeException exception) {
            throw new RuntimeException(exception);
        }
    }

    public List<DetalheVendaDTO> listar() {
        return vendaRepository.findAll()
                .stream()
                .map(vendaMapper::vendaEntityParaDetalheVendaDto)
                .toList();
    }

    public DetalheVendaDTO pegarPorId(Long id) {
        return vendaMapper.vendaEntityParaDetalheVendaDto(vendaRepository.findById(id).orElseThrow(() -> new RuntimeException("Registro não encontrado")));
    }

    public DetalheVendaDTO editar(Long id, CriarVendaDTO vendaDTO) {
        return vendaRepository.findById(id).map(vendaAtual -> {
            vendaAtual.setRestaurante(vendaDTO.restaurante());
            vendaAtual.setFuncionario(vendaDTO.funcionario());
            vendaAtual.setProdutos(vendaDTO.produtosEntities());
            vendaAtual.setSomaTotalDaListaDeProdutos(somaProdutosVenda(vendaDTO.produtosEntities()));
            vendaAtual.setFormaDePagamento(vendaDTO.formaDePagamento());

            return vendaMapper.vendaEntityParaDetalheVendaDto(vendaRepository.save(vendaAtual));
        }).orElseThrow(() -> new RuntimeException("Registro não encontrado"));
    }

    public void excluir(Long id) {
        vendaRepository.delete(vendaRepository.findById(id).orElseThrow(() -> new RuntimeException("Registro não encontrado")));
    }

    private double somaProdutosVenda(List<ProdutosEntity> produtos) {
        return produtos.stream()
                .map(ProdutosEntity::getPrecoVenda)
                .reduce(0.0, Double::sum);
    }
}
