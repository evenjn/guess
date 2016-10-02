/**
 *
 * Copyright 2016 Marco Trevisan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.github.evenjn.align;

public class TupleAlignmentNode {
	/* This is a unique id that identifies the pair [a b] where a is a symbol
	 *  above and b is a finite sequence of symbols below.
	 */
	public int[][] incoming_edges; /* x, y, encout*/
	public int number_of_incoming_edges;
	boolean is_reachable_from_beginning;
	boolean is_reachable_from_end;
	
	public int a;
	public int b;
	
}