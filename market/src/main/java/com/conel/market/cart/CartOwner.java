package com.conel.market.cart;

public record CartOwner(String userId,String guestToken) {

    public static CartOwner ofUser(String userId){
        return new CartOwner(userId,null);
    }

    public static CartOwner ofGuest(String guestToken){
        return new CartOwner(null,guestToken);
    }

    public boolean isGuest(){
        return userId==null;
    }
}
