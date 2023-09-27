package com.basic4gl.library.desktopgl.soundengine.util;

import static org.lwjgl.stb.STBVorbis.*;

public final class STBVorbisUtil {
    public static String getVorbisFileErrorString(int error) {
        switch (error) {
            case VORBIS__no_error: return "STBVorbis: No error";
            case VORBIS_bad_packet_type: return "STBVorbis: Bad packet type";
            case VORBIS_cant_find_last_page: return "STBVorbis: Can't find last page";
            case VORBIS_continued_packet_flag_invalid: return "STBVorbis: Continued packet flag invalid";
            case VORBIS_feature_not_supported: return "STBVorbis: Feature not supported";
            case VORBIS_file_open_failure: return "STBVorbis: File open failure";
            case VORBIS_incorrect_stream_serial_number: return "STBVorbis: Incorrect stream serial number";
            case VORBIS_invalid_api_mixing: return "STBVorbis: Invalid api mixing";
            case VORBIS_invalid_first_page: return "STBVorbis: Invalid first page";
            case VORBIS_invalid_setup: return "STBVorbis: Invalid setup";
            case VORBIS_invalid_stream: return "STBVorbis: Invalid stream";
            case VORBIS_invalid_stream_structure_version: return "STBVorbis: Invalid stream structure version";
            case VORBIS_missing_capture_pattern: return "STBVorbis: Missing capture pattern";
            case VORBIS_need_more_data: return "STBVorbis: Need more data";
            case VORBIS_outofmem: return "STBVorbis: Out of memory";
            case VORBIS_seek_failed: return "STBVorbis: Seek failed";
            case VORBIS_seek_invalid: return "STBVorbis: Seek invalid";
            case VORBIS_seek_without_length: return "STBVorbis: Seek without length";
            case VORBIS_too_many_channels: return "STBVorbis: Too many channels";
            case VORBIS_unexpected_eof: return "STBVorbis: Unexpected eof";
            default:                    return "STBVorbis error";
        }
    }
}
