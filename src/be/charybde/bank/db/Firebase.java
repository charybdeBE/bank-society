//package be.charybde.bank.db;
//
//import be.charybde.bank.entities.Account;
//import com.google.api.core.ApiFuture;
//import com.google.api.core.ApiFutureToListenableFuture;
//import com.google.api.core.ApiFutures;
//import com.google.api.core.SettableApiFuture;
//import com.google.auth.oauth2.GoogleCredentials;
//import com.google.cloud.firestore.DocumentSnapshot;
//import com.google.common.util.concurrent.Futures;
//import com.google.common.util.concurrent.ListenableFuture;
//import com.google.common.util.concurrent.ListenableFutureTask;
//import com.google.firebase.FirebaseApp;
//import com.google.firebase.FirebaseOptions;
//import com.google.firebase.database.*;
//import com.google.protobuf.Api;
//import com.sun.jmx.snmp.tasks.TaskServer;
//import javafx.concurrent.Task;
//import org.bukkit.plugin.java.JavaPlugin;
//
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.lang.reflect.Array;
//import java.util.*;
//import java.util.concurrent.*;
//
//
//public class Firebase {
//
//    public Firebase(JavaPlugin plugin) {
//        FileInputStream serviceAccount = null;
//        try {
//            serviceAccount = new FileInputStream(plugin.getDataFolder() + "/key.json");
//            FirebaseOptions options = new FirebaseOptions.Builder()
//                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
//                    .setDatabaseUrl("https://bccmc-fad6c.firebaseio.com")
//                    .build();
//
//            FirebaseApp.initializeApp(options);
//            DatabaseReference ref = FirebaseDatabase.getInstance()
//                    .getReference("accounts");
//            ref.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    Object document = dataSnapshot.getValue();
//                    System.out.println(document);
//                }
//
//                @Override
//                public void onCancelled(DatabaseError error) {
//                }
//            });
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void read(String a) {
//        final FirebaseDatabase database = FirebaseDatabase.getInstance();
//        DatabaseReference ref = database.getReference("/accounts/" + a);
//        ref.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                System.out.println("ABCD");
//                Object doc = dataSnapshot.child("amount").getValue();
//                System.out.println("aa");
//                System.out.println(doc);
//
//                try {
//                    DummyAccount acc = dataSnapshot.getValue(DummyAccount.class);
//                    System.out.println(acc);
//                    System.out.println("BBBBBBBBBB" + acc.amount);
//                } catch (Exception e) {
//                    System.out.println(e.getMessage());
//                    for (StackTraceElement es : e.getStackTrace()) {
//                        System.out.println(es.toString());
//                    }
//                }
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                System.out.println("The read failed: " + databaseError.getCode());
//            }
//        });
//
//
//    }
//
//    public static void writeAccount(Account acc) {
//        final FirebaseDatabase database = FirebaseDatabase.getInstance();
//        DatabaseReference ref = database.getReference("accounts");
//
//        Map<String, Object> map = new HashMap<>();
//        map.put(acc.getName().toLowerCase(), acc);
//        ref.updateChildrenAsync(map);
//
//        DatabaseReference refUser = database.getReference("users");
//        Map<String, Object> mapUsers = new HashMap<>();
//        acc.getAuthorizedPlayers().forEach((player) -> {
//            Map<String, Object> tmp = new HashMap<>();
//            tmp.put(acc.getName().toLowerCase(), true);
//            refUser.child(player.toLowerCase()).updateChildrenAsync(tmp);
//        });
//        refUser.updateChildrenAsync(mapUsers);
//    }
//
//    public static void readAccountByAuth(String player, SettableCallable<List<Account>> callback) {
//        final FirebaseDatabase database = FirebaseDatabase.getInstance();
//        System.out.println(player);
//        DatabaseReference ref = database.getReference("");
//        ref.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                try {
//                    DataSnapshot user = dataSnapshot.child("users/" + player);
//                    List<Account> res = new ArrayList<>();
//                    user.getChildren().forEach(account -> {
//                        DataSnapshot dataAccount = dataSnapshot.child("accounts/" + account.getKey());
//                        res.add(dataAccount.getValue(Account.class));
//                    });
//                    System.out.println(res);
//                    callback.setResult(res);
//                    callback.call();
//                } catch (Exception e) {
//                    System.out.println(e.getMessage());
//                    for (StackTraceElement es : e.getStackTrace()) {
//                        System.out.println(es.toString());
//                    }
//                }
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                System.out.println("The read failed: " + databaseError.getCode());
//            }
//        });
//    }
//
//    public static void readAllAccounts(SettableCallable<List<Account>> callback) {
//        final FirebaseDatabase database = FirebaseDatabase.getInstance();
//        DatabaseReference ref = database.getReference("accounts/");
//        ref.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                try {
//                    List<Account> res = new ArrayList<>();
//                    dataSnapshot.getChildren().forEach(account -> {
//                        res.add(account.getValue(Account.class));
//                    });
//                    System.out.println(res);
//                    callback.setResult(res);
//                    callback.call();
//                } catch (Exception e) {
//                    System.out.println(e.getMessage());
//                    for (StackTraceElement es : e.getStackTrace()) {
//                        System.out.println(es.toString());
//                    }
//                }
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                System.out.println("The read failed: " + databaseError.getCode());
//            }
//        });
//    }
//
//    public static Future<Account> readAccountByName(String name) {
//        return Firebase.readAccountByName(name, null);
//    }
//
//    public static Future<Account> readAccountByName(String name, SettableCallable<Account> callback) {
//        final FirebaseDatabase database = FirebaseDatabase.getInstance();
//        System.out.println("RABT a " + name);
//        DatabaseReference ref = database.getReference("/accounts/" + name.toLowerCase());
//        final SettableApiFuture<Account> fut = SettableApiFuture.<Account>create();
//        ref.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                try {
//                    System.out.println("RABT " + dataSnapshot.getValue());
//                    Account acc = dataSnapshot.getValue(Account.class);
//                    fut.set(acc);
//                    if (callback != null) {
//                        callback.setResult(acc);
//                        callback.call();
//                    }
//                } catch (Exception e) {
//                    System.out.println(e.getMessage());
//                    for (StackTraceElement es : e.getStackTrace()) {
//                        System.out.println(es.toString());
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                System.out.println("The read failed: " + databaseError.getCode());
//            }
//        });
//        return fut;
//    }
//
//
//    public static void addOwners(String accountName, List<String> owners){
//        final FirebaseDatabase database = FirebaseDatabase.getInstance();
//        DatabaseReference ref = database.getReference("users");
//
//        Map<String, Object> mapAccount = new HashMap<>();
//        mapAccount.put(accountName, true);
//        owners.forEach((owner) -> {
//            ref.child(owner).updateChildrenAsync(mapAccount);
//        });
//    }
//
//    public static void hasAccess(String player, String account){
//        // TODO
//    }
//
//    public static void removeOwners(String accountName, List<String> owners){
//        final FirebaseDatabase database = FirebaseDatabase.getInstance();
//        DatabaseReference ref = database.getReference("users");
//        owners.forEach((owner) -> {
//            ref.child(owner).child(accountName).removeValueAsync();
//        });
//    }
//
//
//    public void delete() {
//        FirebaseApp.getInstance().delete();
//    }
//
//}
//
//class DummyAccount {
//    public int amount;
//    public String institution;
//
//    public DummyAccount() {
//
//    }
//
//    public DummyAccount(int amount) {
//        this.amount = amount;
//    }
//
//    public DummyAccount(int amount, String institution) {
//        this.amount = amount;
//        this.institution = institution;
//    }
//}