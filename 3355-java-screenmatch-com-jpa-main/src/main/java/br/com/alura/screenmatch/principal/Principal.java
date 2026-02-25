package br.com.alura.screenmatch.principal;
import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=7b8ca78e";
    private SerieRepository repository;

    List<DadosSerie> series = new ArrayList<>();
    private List<Serie> seriesLista = new ArrayList<>();

    public Principal(SerieRepository repository) {
        this.repository = repository;
    }

    public void exibeMenu() {
        var opcao = -1;
        while (opcao != 0) {

            var menu = """
                    1 - Buscar séries
                    2 - Buscar episódios
                    3 - Lista series
                    4 - Dados da series
                    5 - Buscar Série por Titulo
                    6 - Buscar Série por Ator
                    7 - As Melhores Seríes
                    8 - Buscar Séries por Categoria 
                    9 - Buscar Séries por Quantidade de Temporada
                    
                    
                    0 - Sair                                 
                    """;

            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listaSeriesBuscadas();
                    break;
                case 4:
                    getDadosSerie();
                    break;
                case 5:
                    buscarTitulo();
                    break;
                case 6:
                    BuscarSeriePorAtor();
                    break;
                case 7:
                    TopSeries();
                    break;
                case 8:
                    BuscarSeriePorCategoria();
                    break;
                case 9:
                    BuscarSeriePorquantidadeDeTemporadas();
                    break;    
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }

    private void BuscarSeriePorquantidadeDeTemporadas() {
        System.out.println("Qual aquantidade de temporadas você desejar que a série tenha ? ");
        var numeroTemporada = leitura.nextInt();
        System.out.println("Qual a nota de avaliação da série? ");
        var Avaliacao = leitura.nextDouble();

        try {
            List<Serie> seriesT = repository.findBytotalTemporadasLessThanEqualAndAvaliacaoGreaterThanEqual(numeroTemporada,Avaliacao);
            seriesT.forEach( st ->
                    System.out.println("A seria que você busca são essas: " + st.getTitulo() + " Avaliação: " + st.getAvaliacao()));

        } catch (Exception e) {
            System.out.println("Serie não encontrada");
        }

    }

    private void BuscarSeriePorCategoria() {
        System.out.println("Qual a categoria da Série que você gostaria de ver ?");
        var nomeCategoria = leitura.nextLine();

        try {
            Categoria categoria = Categoria.fromPortuges(nomeCategoria); /*Enum*/
            List<Serie> serieCategoria = repository.findByGenero(categoria);
            System.out.println("Séries com a categoria: " + nomeCategoria);
            serieCategoria.forEach(sc ->
                    System.out.println("Serie: " + sc.getTitulo() + " Categoria: " + sc.getGenero())
            );

        } catch (Exception e) {
            System.out.println("Infelizmente não encontramos série com a categoria solicitada! ");
        }
    }

    private void TopSeries() {
        List<Serie> seriesTop = repository.findTop5ByOrderByAvaliacaoDesc();
        seriesTop.forEach( st ->
                System.out.println("as melhores series são: " + st.getTitulo() + " com  a nota de avalição: " + st.getAvaliacao())
        );
    }

    private void BuscarSeriePorAtor() {
        System.out.println("Digite o nome do ator para achar a série");
        var nomeAtor = leitura.nextLine();
        System.out.println("Qual o valor da avaliação: ");
        var Avaliacao = leitura.nextDouble();
        List<Serie> seriesEncontradas = repository.findByatoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeAtor,Avaliacao);
        System.out.println("essa é a serie do ator " + nomeAtor);
        seriesEncontradas.forEach( s ->
                System.out.println(s.getTitulo() + " avaliacao " + s.getAvaliacao()));

    }
    private void buscarTitulo() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        Optional<Serie> serieBuscada = repository.findByTituloContainingIgnoreCase(nomeSerie);
         if ((serieBuscada.isPresent())){
             System.out.println("Serie encontrada " + serieBuscada.get());
         }else {
             System.out.println("Por gentileza verifique o nome da série");

         }
    }

    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        Serie serie1 = new Serie(dados);
        repository.save(serie1);
        System.out.println(serie1);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        System.out.println("Nome: " +  dados.titulo() + " Gênero: " + dados.genero() + " sinpse: " + dados.sinopse());
        return dados;
    }

    private void buscarEpisodioPorSerie(){
        listaSeriesBuscadas();
        System.out.println("Escolha uma Série pelo nome: ");
        var nomeSerie = leitura.nextLine();
        Optional<Serie> serieBuscada = repository.findByTituloContainingIgnoreCase(nomeSerie);

        if (serieBuscada.isPresent()){
            var serieEncontrada = serieBuscada.get();
            List<DadosTemporada> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumo.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            temporadas.forEach(System.out::println);

            List<Episodio> episodiosDasSeries = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());
            serieEncontrada.setEpisodios(episodiosDasSeries);
            repository.save(serieEncontrada);
        }
    }

    private void listaSeriesBuscadas(){
        seriesLista = repository.findAll();
        seriesLista.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);

    }
}

