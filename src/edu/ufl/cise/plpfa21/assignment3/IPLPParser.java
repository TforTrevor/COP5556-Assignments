package edu.ufl.cise.plpfa21.assignment3;

import edu.ufl.cise.plpfa21.assignment3.ast.IASTNode;

public interface IPLPParser {

	IASTNode parse() throws Exception;
	
}
