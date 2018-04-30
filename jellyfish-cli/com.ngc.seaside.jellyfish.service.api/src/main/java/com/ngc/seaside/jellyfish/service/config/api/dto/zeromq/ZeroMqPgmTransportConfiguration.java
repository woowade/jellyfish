package com.ngc.seaside.jellyfish.service.config.api.dto.zeromq;

import com.google.common.base.Objects;

import com.ngc.seaside.jellyfish.service.config.api.dto.NetworkInterface;

/**
 * Defines the configuration for a link that should use ZeroMQ's PGM transport mechanism. This is a reliable protocol
 * which uses UDP. In most cases, EPGM should be preferred.
 */
public class ZeroMqPgmTransportConfiguration extends ZeroMqConfiguration {

   private String groupAddress;
   private int port;
   private NetworkInterface sourceInterface;
   private NetworkInterface targetInterface;

   /**
    * An IPv4 or IPv6 formatted multicast group address that should be used to implement the link.
    * This should not be a DNS address or hostname.
    */
   public String getGroupAddress() {
      return groupAddress;
   }

   public ZeroMqPgmTransportConfiguration setGroupAddress(String groupAddress) {
      this.groupAddress = groupAddress;
      return this;
   }

   /**
    * A port number.
    */
   public int getPort() {
      return port;
   }

   public ZeroMqPgmTransportConfiguration setPort(int port) {
      this.port = port;
      return this;
   }

   /**
    * The network interface of the source of the link to use for the connection.
    */
   public NetworkInterface getSourceInterface() {
      return sourceInterface;
   }

   public ZeroMqPgmTransportConfiguration setSourceInterface(NetworkInterface sourceInterface) {
      this.sourceInterface = sourceInterface;
      return this;
   }

   /**
    * The network interface of the target of the link to use for the connection.
    */
   public NetworkInterface getTargetInterface() {
      return targetInterface;
   }

   public ZeroMqPgmTransportConfiguration setTargetInterface(NetworkInterface targetInterface) {
      this.targetInterface = targetInterface;
      return this;
   }

   @Override
   public boolean equals(Object o) {
      if (!(o instanceof ZeroMqPgmTransportConfiguration)) {
         return false;
      }
      ZeroMqPgmTransportConfiguration that = (ZeroMqPgmTransportConfiguration) o;
      return Objects.equal(this.getConnectionType(), that.getConnectionType())
            && Objects.equal(this.getGroupAddress(), that.getGroupAddress())
            && Objects.equal(this.getPort(), that.getPort())
            && Objects.equal(this.getSourceInterface(), that.getSourceInterface())
            && Objects.equal(this.getTargetInterface(), that.getTargetInterface());
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.getConnectionType(),
                              this.getGroupAddress(),
                              this.getPort(),
                              this.getSourceInterface(),
                              this.getTargetInterface());
   }

   @Override
   public String toString() {
      return "ZeroMqPgmTransportConfiguration[connectionType=" + this.getConnectionType()
            + ",groupAddress=" + this.getGroupAddress()
            + ",port=" + this.getPort()
            + ",sourceInterface=" + this.getSourceInterface().getName()
            + ",targetInterface=" + this.getTargetInterface().getName()
            + "]";
   }
}
