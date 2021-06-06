package it.polito.tdp.PremierLeague.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.polito.tdp.PremierLeague.db.PremierLeagueDAO;

public class Model {
	private PremierLeagueDAO dao;
	private Map<Integer,Player> idMap;
	private Graph<Player,DefaultWeightedEdge> grafo;
	private List<Player> dreamTeam;
	private int bestSomma;
	//private List<Player> blackList;
	
	
	public Model() {
		dao=new PremierLeagueDAO();
	}
	
	public void creaGrafo(double x) {
		grafo= new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
		idMap=new HashMap<>();
		dao.listAllPlayers(idMap);
		
		Graphs.addAllVertices(grafo,dao.getGiocatori(idMap, x));
		for(Adiacenza a: dao.getAdiacenze(idMap)) {
			if(grafo.vertexSet().contains(a.getP1()) && grafo.vertexSet().contains(a.getP2()))
			Graphs.addEdge(grafo, a.getP1(), a.getP2(), a.getPeso());
		}
		
	}
	
	public String getBestPlayer() {
		if(grafo!= null) {
		Player bestPlayer=null;
		int totPlayer=0;
		
		for(Player p: grafo.vertexSet()) {
			if(grafo.outDegreeOf(p)>totPlayer) {
				bestPlayer=p;
			totPlayer = grafo.outDegreeOf(p);
			}
		}
		
		String s=" TOP PLAYER : "+bestPlayer.getPlayerID() +" "+bestPlayer.getName()+"\n";
		List<Adiacenza> result= new ArrayList<>();
		for(DefaultWeightedEdge uscita :this.grafo.outgoingEdgesOf(bestPlayer)) {
			result.add(new Adiacenza(grafo.getEdgeSource(uscita),grafo.getEdgeTarget(uscita),(int)grafo.getEdgeWeight(uscita)));
		}
		Collections.sort(result);
		s+="GIOCATORI BATTUTI: \n";
		for(Adiacenza a: result) {
			s+= a.getP2().getPlayerID()+" "+a.getP2().getName()+" "+a.getPeso()+"\n";
		}
		return s;
		}
		else
			return "CREA PRIMA IL GRAFO!";
	}
	
	public int getVertexSize() {
		return grafo.vertexSet().size();
	}
	public int getEdgeSize() {
		return grafo.edgeSet().size();
	}
	
	public List<Player> getDreamTeam(int k ){
		dreamTeam= new ArrayList<>();
		bestSomma=0;
		List<Player> parziale= new ArrayList<>();
		List<Player>blackList= new ArrayList<>();
		cercaSoluzione(parziale,k,blackList);
		return dreamTeam;
		
	}

	private void cercaSoluzione(List<Player> parziale,int k,List<Player>blackList) {
		// caso terminale
		if(parziale.size() == k) {
			int somma=0;
			for(Player p: parziale) {
				somma+=calcolaMinutaggio(p);
			}
			if (somma>bestSomma) {
				dreamTeam=new ArrayList<>(parziale);
				bestSomma=somma;
			}
		}else {
			for(Player p: this.grafo.vertexSet()) {
				if(!blackList.contains(p) && !parziale.contains(p)) {
					parziale.add(p);
					for(DefaultWeightedEdge uscita :this.grafo.outgoingEdgesOf(p)) {
						blackList.add(grafo.getEdgeTarget(uscita));
					}
					cercaSoluzione(parziale,k,blackList);
					//backtracking
					parziale.remove(p);
					for(DefaultWeightedEdge uscita :this.grafo.outgoingEdgesOf(p)) {
						blackList.remove(grafo.getEdgeTarget(uscita));
					}
				}
			}
		}
		
	}

	private int calcolaMinutaggio(Player p) {
		 int daSottrarre=0;
		 int daSommare=0;
		for(DefaultWeightedEdge uscita :this.grafo.outgoingEdgesOf(p)) {
			daSommare+=this.grafo.getEdgeWeight(uscita);
		}
		for(DefaultWeightedEdge entrata :this.grafo.incomingEdgesOf(p)) {
			daSottrarre+=this.grafo.getEdgeWeight(entrata);
		}
		return (daSommare-daSottrarre);
	}
}
