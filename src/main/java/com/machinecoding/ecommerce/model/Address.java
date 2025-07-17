package com.machinecoding.ecommerce.model;

/**
 * Represents an address for shipping and billing.
 */
public class Address {
    private final String street;
    private final String city;
    private final String state;
    private final String zipCode;
    private final String country;
    private final AddressType type;
    
    public Address(String street, String city, String state, String zipCode, String country, AddressType type) {
        this.street = street != null ? street.trim() : "";
        this.city = city != null ? city.trim() : "";
        this.state = state != null ? state.trim() : "";
        this.zipCode = zipCode != null ? zipCode.trim() : "";
        this.country = country != null ? country.trim() : "";
        this.type = type != null ? type : AddressType.SHIPPING;
    }
    
    // Getters
    public String getStreet() { return street; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getZipCode() { return zipCode; }
    public String getCountry() { return country; }
    public AddressType getType() { return type; }
    
    public String getFormattedAddress() {
        return String.format("%s, %s, %s %s, %s", street, city, state, zipCode, country);
    }
    
    @Override
    public String toString() {
        return String.format("Address{%s, type=%s}", getFormattedAddress(), type);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Address address = (Address) obj;
        return street.equals(address.street) && 
               city.equals(address.city) && 
               state.equals(address.state) && 
               zipCode.equals(address.zipCode) && 
               country.equals(address.country);
    }
    
    @Override
    public int hashCode() {
        return (street + city + state + zipCode + country).hashCode();
    }
}