package com.basic4gl.library.netlib4games;

/**
 * Used to track which packets have already been received by their ID.
 * Current implementation as an array of bools.
 * Could be replaced by a proper bit array as an optimisation.
 */
public class NetRevolvingBitBuffer {
    int	m_size;
    boolean[] m_data;

    // Position pointers
    int	m_hi,
            m_hiRevolving,
            m_low,
            m_lowRevolving;

    public NetRevolvingBitBuffer (int size, int initialTop) {
        m_size = (size);
                m_hi= (initialTop);
                m_low= (initialTop);
                m_hiRevolving= (0);
                m_lowRevolving =(0);

            assert (m_size > 0);
            m_data = new boolean [m_size];
    }
    public void dispose() {

    }

    int	Hi ()	{ return m_hi; }
    int	Low ()	{ return m_low; }

    boolean InRange (long index) {
        return (index >= m_low && index < m_hi);
    }
    int Revolving (int index) {
        assert (InRange (index));
        return (m_lowRevolving + index - m_low) % m_size;
    }
    boolean Value (int index) {
        return m_data [Revolving (index)];
    }
    void set(int index, boolean value) {
        m_data [Revolving (index)] = value;
    }

    /**
     *
     * @return true if falses removed
     */
    boolean SetTop (int index, boolean initialValue) {
        boolean truesRemoved	= false;
        boolean falsesRemoved	= false;

        // Remove low array elements
        if (index > m_size) {
            int lowIndex = index - m_size;
            while (m_low < lowIndex) {

                // Calculate value removed.
                boolean value = m_low < m_hi ? m_data [m_lowRevolving] : initialValue;

                // Update flags accordingly
                truesRemoved  = truesRemoved  ||  value;
                falsesRemoved = falsesRemoved || !value;

                // Update low poiners
                if (++m_lowRevolving >= m_size)
                    m_lowRevolving = 0;
                m_low++;
            }
        }

        // Initialise high array elements
        while (m_hi < index) {

            // Initialise data
            if (m_hi >= m_low)
                m_data [m_hiRevolving] = initialValue;

            // Update high pointers
            if (++m_hiRevolving >= m_size)
                m_hiRevolving = 0;
            m_hi++;
        }

        return falsesRemoved;
    }
}
