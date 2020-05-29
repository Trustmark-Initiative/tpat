package tmf.host

/**
 * Chunks the content of binary data, so that you don't violate max packet sizes, and so you can do things
 * like stream the data.
 */
class BinaryDataChunk implements Comparable<BinaryDataChunk> {
    final static int MAX_CHUNK_SIZE = 250000; // 250K max size.


    static transients = ['content', 'MAX_CHUNK_SIZE']

    static belongsTo = [
        binaryData: BinaryData
    ]

    /**
     * Chunks have an assumed ordering.  This field will preserve that order.
     */
    Integer sequenceNumber = -1
    /**
     * The raw binary data.
     */
    byte[] byteData   // The raw byte content

    static constraints = {
        binaryData(nullable: false);
        byteData(nullable: false, maxSize: MAX_CHUNK_SIZE)
        sequenceNumber(nullable: false);
    }

    static mapping = {
        table(name:'binary_data_chunk')
        sequenceNumber(column: 'sequence_number')
        binaryData(column: 'binary_data_ref')
        byteData(column: 'byte_data', type: 'binary')
    }

    @Override
    int compareTo(BinaryDataChunk o) {
        return this.sequenceNumber.compareTo(o.sequenceNumber);
    }

}//end BinaryData
