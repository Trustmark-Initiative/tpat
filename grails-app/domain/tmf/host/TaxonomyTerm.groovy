package tmf.host
/**
 * Represents a Taxonomy Term.  Taxonomy terms are keywords in a tree structure.
 */
class TaxonomyTerm implements Comparable<TaxonomyTerm> {

    public static List<TaxonomyTerm> getTopLevelTerms(){
        def terms = [];
        def topTerms = TaxonomyTerm.findAllByParent(null);
        terms.addAll(topTerms);
        Collections.sort(topTerms);
        return terms;
    }


    static belongsTo = [parent: TaxonomyTerm]

    String name

    static constraints = {
        parent(nullable: true)
        name(nullable: false, blank: false, maxSize: 128, unique: true)
    }

    static mapping = {
        table 'taxonomy_term'
        parent(column: 'parent_ref')
        name(column: 'name')
    }

    /**
     * Returns the child terms for the given term.
     */
    public List<TaxonomyTerm> getChildren(){
        def terms = TaxonomyTerm.findAllByParent(this);
        def sortedTerms = [];
        sortedTerms.addAll(terms);
        Collections.sort(terms);
        return terms;
    }

    public String toString(){
        return name;
    }

    public boolean equals(Object other){
        if( other && other instanceof TaxonomyTerm ){
            return this.name.equalsIgnoreCase(other.name);
        }
        return false;
    }

    public int hashCode(){
        return name.hashCode();
    }

    @Override
    public int compareTo(TaxonomyTerm other){
        return this.name.compareToIgnoreCase(other.name);
    }

    public Map toJsonMap(boolean full){
        Map json = [name: this.name]
        if( this.parent )
            json.put("parent", [name: this.parent.name])

        List<TaxonomyTerm> children = this.getChildren();
        if( children != null && children.size() > 0 ) {
            json.put("children", []);
            for (TaxonomyTerm child : children) {
                if( full ) {
                    json.children.add(child.toJsonMap(true));
                }else{
                    json.children.add([name: child.name]);
                }
            }
        }

        return json;
    }

}