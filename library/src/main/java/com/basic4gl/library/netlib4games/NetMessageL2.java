package com.basic4gl.library.netlib4games;


/**
 * Layer 2 network message
 */
public class NetMessageL2 {
        int						channel;
        boolean					reliable;
    boolean					ordered;
    boolean                    smoothed;
        int messageIndex;   // unsigned long
        int			reliableIndex; // unsigned long
        int			packetCount;
        long			tickCount;
        int			receivedCount;
        int						dataSize;
        boolean					tickCountRegistered;
        NetSimplePacket[]			packets;

        NetMessageL2 (	int _channel,
        boolean _reliable,
        boolean _smoothed,
        boolean _ordered,
        int _messageIndex,
        int _reliableIndex,
        int _packetCount,
        long _tickCount) {

        }
        public void dispose () {

        // Delete packets
        if (packets != null) {

        // Delete assigned packets
        for (int i = 0; i < packetCount; i++) {
            if (packets[i] != null) {
                packets[i].dispose();
            }
        }

        // Delete packet array
        packets = null;
        }
        }

        boolean Complete () { return receivedCount >= packetCount; }

        boolean Buffer (NetSimplePacket packet, int packetIndex) {
            assert (packet != null);

            if (packetIndex >= 0 && packetIndex < packetCount && packets [packetIndex] == null) {

                // Add packet to message
                packets [packetIndex] = packet;
                receivedCount++;
                dataSize += packet.size;
                return true;
            }
            else {
                // Packet already received... or index is bad
                return false;
            }
        }

    /**
     *
     * @param data
     * @param offset
     * @param size
     * @return size
     */
        int CopyData (byte [] data, int offset, int size) {
            assert (data != null);
            assert (offset <= dataSize);

            // Adjust size
            if (offset + size > dataSize)
                size = dataSize - offset;

            // Find start packet
            int packet = 0;
            while (offset > packets [packet].size) {
                offset -= packets [packet].size;
                packet++;
            }

            // Copy data from packets
            int destOffset = 0, left = size;
            while (left > 0) {
                int copySize = Math.min (left, packets [packet].size - offset);

                // Copy data
                System.arraycopy(packets [packet].data, offset, data, destOffset, copySize);

                left		-= copySize;
                destOffset	+= copySize;
                packet++;
                offset		= 0;
            }
            return size;
        }
        };