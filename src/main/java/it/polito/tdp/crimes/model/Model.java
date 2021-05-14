package it.polito.tdp.crimes.model;

import java.util.LinkedList;
import java.util.List;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.crimes.db.EventsDao;

public class Model {
	
	
	// NON CREIAMO LA IDMAP PERCHE' I VERTICI 
	// SONO DI TIPO STRINGA E NON DI OGGETTI 
	
	private SimpleWeightedGraph<String, DefaultWeightedEdge> grafo;
	private EventsDao dao;
	private List<String> percorsoMigliore; 
	
	public Model(){
		dao = new EventsDao();
	}
	public void creaGrafo(String categoria, int mese) {
		
		this.grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		
		// AGGIUNGO I VERTICI 
		Graphs.addAllVertices(grafo, dao.getVertici(categoria,mese));
		
		
		// AGGIUNGO GLI ARCHI 
		for (Adiacenza a: dao.getAdiacenze(categoria, mese)) {
			
			// se l'arco non c'è ancora ( == null è un controllo ulteriore)
			if(this.grafo.getEdge(a.getV1(),  a.getV2()) == null) {
				Graphs.addEdgeWithVertices(grafo, a.getV1(), 
						a.getV2(),a.getPeso());
				
			}
		}
		
		System.out.println("# vertici: "+this.grafo.vertexSet().size());
		System.out.println("# archi: "+this.grafo.vertexSet().size());
	}
	
	// SERVE PER IL PUNTO D
	public List<Adiacenza> getArchi(){
		// calcolo il peso medio degli archi presenti nel grafo
		double pesoMedio = 0.0;
		for (DefaultWeightedEdge e: this.grafo.edgeSet()) {
			pesoMedio += this.grafo.getEdgeWeight(e);
		}
		pesoMedio = pesoMedio / this.grafo.edgeSet().size();
		// filtro gli archi, tenendo solo quelli che hanno peso maggiore del peso medio
		List<Adiacenza> result = new LinkedList<>();
		for (DefaultWeightedEdge e: this.grafo.edgeSet()) {
			if(this.grafo.getEdgeWeight(e) > pesoMedio)
				result.add(new Adiacenza(this.grafo.getEdgeSource(e),
						this.grafo.getEdgeTarget(e), this.grafo.getEdgeWeight(e)));
			
		}
		return result; 
	}
	
	
	// DEVO TROVARE IL PERCORSO PIU' LUNGO 
	public List<String> trovaPercorso(String sorgente, String destinazione){
		
		this.percorsoMigliore = new LinkedList<>();
		List<String> parziale = new LinkedList<>();
		
		// sicuramente parziale contiene il vertice sorgente
		parziale.add(sorgente);
		cerca(destinazione, parziale);
		return this.percorsoMigliore;
		
	}
	
	private void cerca(String destinazione, List<String> parziale) {
		// caso terminale
		// se il vertice in ultima posizione coincide con la destinazione
		if(parziale.get(parziale.size()-1).equals(destinazione)) {
			// se parziale è migliore della soluzione migliore fino ad ora
			if (parziale.size() > this.percorsoMigliore.size()) {
				// la sovrascrivo
				this.percorsoMigliore = new LinkedList<>(parziale);
				
			}
			return ; 
		}
		// ... altrimenti
		// --> scorro i vicini dell'ultimo inserito
		// --> e provo ad aggiungerli uno ad uno 
		for (String vicino : Graphs.neighborListOf(grafo, 
				parziale.get(parziale.size()-1))) {
		
			// vogliamo trovare un cammino aciclico 
			// quindi non possiamo mettere un vertice già visitato
			if(!parziale.contains(vicino)) {
				parziale.add(vicino);
				cerca(destinazione, parziale);
				parziale.remove(parziale.size()-1);
			}
		}
	}
	public List<String> getCategorie(){
		return dao.getCategorie();
	}
}
