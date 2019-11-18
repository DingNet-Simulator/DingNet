package util;

import iot.lora.LoraTransmission;
import iot.networkentity.NetworkEntity;

import java.util.*;

public class Statistic {

    private static Statistic instance = new Statistic();

    // A list representing the power setting of every transmission.
    private final Map<Long, List<List<Pair<Integer,Integer>>>> powerSettingHistory;

    // A list representing the spreading factor of every transmission.
    private final Map<Long, List<List<Integer>>> spreadingFactorHistory;

    // A map with the transmissions received by the entity and if they collided with an other packet.
    private final Map<Long, List<LinkedHashSet<LoraTransmission>>> receivedTransmissions;

    // A list with the transmissions transmitted by the entity
    private final Map<Long, List<List<LoraTransmission>>> sentTransmissions;

    private Statistic() {
        powerSettingHistory = new HashMap<>();
        spreadingFactorHistory = new HashMap<>();
        receivedTransmissions = new HashMap<>();
        sentTransmissions = new HashMap<>();
    }

    public static Statistic getInstance() {
        return instance;
    }

    public void addPowerSettingEntry(NetworkEntity networkEntity, Pair<Integer,Integer> entry) {
        addPowerSettingEntry(networkEntity.getEUI(), entry);
    }

    public void addPowerSettingEntry(long networkEntity, Pair<Integer,Integer> entry) {
        initIfAbsent(powerSettingHistory, networkEntity);
        var lists = powerSettingHistory.get(networkEntity);
        ListHelper.getLast(lists).add(entry);
    }

    public void addSpreadingFactorEntry(NetworkEntity networkEntity, int entry) {
        addSpreadingFactorEntry(networkEntity.getEUI(), entry);
    }

    public void addSpreadingFactorEntry(long networkEntity, int entry) {
        initIfAbsent(spreadingFactorHistory, networkEntity);
        var lists = spreadingFactorHistory.get(networkEntity);
        ListHelper.getLast(lists).add(entry);
    }

    public void addReceivedTransmissionsEntry(NetworkEntity networkEntity, LoraTransmission entry) {
        addReceivedTransmissionsEntry(networkEntity.getEUI(), entry);
    }

    public void addReceivedTransmissionsEntry(long networkEntity, LoraTransmission entry) {
        initIfAbsent(receivedTransmissions, networkEntity);
        var lists = receivedTransmissions.get(networkEntity);
        ListHelper.getLast(lists).add(entry);
    }

    public void addSentTransmissionsEntry(NetworkEntity networkEntity, LoraTransmission entry) {
        addSentTransmissionsEntry(networkEntity.getEUI(), entry);
    }

    public void addSentTransmissionsEntry(long networkEntity, LoraTransmission entry) {
        initIfAbsent(sentTransmissions, networkEntity);
        var lists = sentTransmissions.get(networkEntity);
        ListHelper.getLast(lists).add(entry);
    }

    private <E> void initIfAbsent(Map<Long, List<E>> map, long id) {
        if (!map.containsKey(id)) {
            map.put(id, new LinkedList<E>());
        }
    }

    public void reset() {
        powerSettingHistory.clear();
        spreadingFactorHistory.clear();
        receivedTransmissions.clear();
        sentTransmissions.clear();
    }

    public void addRun() {
        powerSettingHistory.forEach((k,v) -> v.add(new LinkedList<>()));
        spreadingFactorHistory.forEach((k,v) -> v.add(new LinkedList<>()));
        receivedTransmissions.forEach((k,v) -> v.add(new LinkedHashSet<>()));
        sentTransmissions.forEach((k,v) -> v.add(new LinkedList<>()));
    }

    public List<List<Pair<Integer,Integer>>> getPowerSettingHistory(long networkEntity) {
        return powerSettingHistory.get(networkEntity);
    }

    public List<Pair<Integer,Integer>> getPowerSettingHistory(long networkEntity, int run) {
        return getPowerSettingHistory(networkEntity).get(run);
    }

    public List<List<Integer>> getSpreadingFactorHistory(long networkEntity) {
        return spreadingFactorHistory.get(networkEntity);
    }

    public List<Integer> getSpreadingFactorHistory(long networkEntity, int run) {
        return getSpreadingFactorHistory(networkEntity).get(run);
    }

    public List<LinkedHashSet<LoraTransmission>> getReceivedTransmissions(long networkEntity) {
        return receivedTransmissions.get(networkEntity);
    }

    public LinkedHashSet<LoraTransmission> getReceivedTransmissions(long networkEntity, int run) {
        return getReceivedTransmissions(networkEntity).get(run);
    }

    public List<List<LoraTransmission>> getSentTransmissions(long networkEntity) {
        return sentTransmissions.get(networkEntity);
    }

    public List<LoraTransmission> getSentTransmissions(long networkEntity, int run) {
        return getSentTransmissions(networkEntity).get(run);
    }

    public List<Double> getUsedEnergy(long networkEntity, int run) {
        List<Double> usedEnergy = new LinkedList<>();
        int i= 0;
        for(LoraTransmission transmission: getSentTransmissions(networkEntity, run)) {
            usedEnergy.add(Math.pow(10,((double)getPowerSettingHistory(networkEntity, run).get(i).getRight())/10)*transmission.getTimeOnAir()/1000);
            i++;
        }
        return usedEnergy;
    }
}


