package com.ohagner.deviations.domain

class Transport {

    String line
    TransportMode transportMode

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Transport transport = (Transport) o

        if (line != transport.line) return false
        if (transportMode != transport.transportMode) return false

        return true
    }

    int hashCode() {
        int result
        result = line.hashCode()
        result = 31 * result + transportMode.hashCode()
        return result
    }


    @Override
    public String toString() {
        return "Transport{" +
                "line='" + line + '\'' +
                ", transportMode=" + transportMode +
                '}';
    }

}
