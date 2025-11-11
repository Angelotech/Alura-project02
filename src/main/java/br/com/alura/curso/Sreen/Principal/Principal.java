package br.com.alura.curso.Sreen.Principal;

import Model.DadosEpisodios;
import Model.DadosSerie;
import Model.DadosTEmporada;
import Model.Episodio;
import Service.ConsumoAPI;
import Service.ConverteDados;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/*CTRL + ALT + O LIMPA IMPORTS SEM USO */
public class Principal {

    public Scanner serie = new Scanner(System.in);

    private ConsumoAPI consumo = new ConsumoAPI();

    private ConverteDados conversor = new ConverteDados();

    public Scanner leitura = new Scanner(System.in);


    //padrão nomes para constantes o final é para valores que não podem ser modificados
    private String ENDERECO = "https://www.omdbapi.com/?t=";
    private String API_KEY = "&apikey=f87044f2";


    public void exibirMenu() {

        System.out.println("digite o nome da série para ser realizado a busca: ");
        var nomeSerie = serie.nextLine();
        var json = consumo.ObterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);

        System.out.println("Sua série foi encontrada: " + dados);

        List<DadosTEmporada> Total = new ArrayList<>();

        System.out.println("------TEMPORADAS----");

        DadosTEmporada temporadas = null;
        for (int i = 1; i <= dados.totalTemporadas(); i++) {
            var jsonTemporadas = consumo.ObterDados(ENDERECO + nomeSerie.replace(" ", "+") + "&season=" + i + API_KEY);
            temporadas = conversor.obterDados(jsonTemporadas, DadosTEmporada.class);
            Total.add(temporadas);
        }

        Total.forEach(System.out::println);
//
//        /*lista dentro de outra listas */
//        /*classificação dos top 5 episodios */
//        List<DadosEpisodios> dadosEpisodios = Total.stream()
//                .flatMap(t -> t.episodios().stream())
//                .collect(Collectors.toList());  /*mutavel*/
//                /*.toList       /* imutavei */
//        dadosEpisodios.stream()
//                .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
//                .peek( e ->  System.out.println("primeiro filtro (N/A) " + e))
//                .sorted(Comparator.comparing(DadosEpisodios::avaliacao).reversed())
//                .peek( e ->  System.out.println("ordenação " + e))
//                .limit(10)
//                .peek( e ->  System.out.println("limite " + e))
//                .map(e -> e.titulo().toUpperCase())
//                .peek( e ->  System.out.println("Mapeamneto " + e))
//                .forEach(System.out::println);

                        /*mostra o episodio de cada temporada*/
        List<Episodio> episodios = Total.stream()
                .flatMap(t -> t.episodios().stream()
                        .map(d -> new Episodio(t.numero(), d))
                ).collect(Collectors.toList());

        episodios.forEach(System.out::println);


                         /*buscar de trecho do titulo*/
        System.out.println("digite o trecho do titulo do episodio");
        var trechoDoTitulo = leitura.nextLine();
        Optional<Episodio> first = episodios.stream()
                .filter(e -> e.getTitulo().toUpperCase().contains(trechoDoTitulo.toUpperCase()))
                .findFirst();
        if(first.isPresent()){
            System.out.println(first);
            System.out.println(" episodio encontrado! ");
            System.out.println("temporada: " + first.get().getTemporada());
        }else {
            System.out.println("episódio não encontrado");
        }



//        System.out.println(" a partir de que ano você desejar ver os episodios? ");
//        var ano  = leitura.nextInt();
//        leitura.nextLine();
//
//        LocalDate databusca = LocalDate.of(ano,1,1);
//
//        /*formatado de horas*/
//        DateTimeFormatter formatado =  DateTimeFormatter.ofPattern("dd/MM/yyyy");
//
//      episodios.stream()
//                .filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(databusca))
//                .forEach( e -> System.out.println(
//                        "temporada: " + e.getTemporada() +
//                                "Episodio: " + e.getTitulo() +
//                                "Data lançamento: "  + e.getDataLancamento().format(formatado)
//                        )
//                );
//
///*        Map<Integer, Double> avaliacaoPorTemporada = episodios.stream()/*agrupamento de dados */
//                .collect(Collectors.groupingBy(Episodio::getTemporada,
//                        Collectors.averagingDouble(Episodio::getAvaliacao)));
//        System.out.println(avaliacaoPorTemporada);












    }





}



