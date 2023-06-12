
import javafx.util.Pair;//多返回值导包
import java.io.*;//导入包含了流式I/O所需要的所有类
import java.util.*;

public class Inventory {
    //创建Inventory类对象
    public String itemQuantity;//货物型号
    public int Quantity;//货物数量
    public String supplier;//供应商编号
    public String description;//货物描述
    public Inventory(String itemQuantity, int Quantity, String supplier, String description) {
        this.itemQuantity = itemQuantity;
        this.Quantity = Quantity;
        this.supplier = supplier;
        this.description = description;
    }

    public static void main(String[] args) throws IOException {
        String s = null;
        List<Inventory> inventoryList = new ArrayList<>();//创建list容器
        Map<String, Integer> itemToIndex = new HashMap<>();//创建map集合

        BufferedReader inventoryReader = new BufferedReader(new FileReader("Inventory.txt"));

        while((s = inventoryReader.readLine()) != null){
            String[] split =  s.split("\t");
            inventoryList.add(new Inventory(split[0], Integer.parseInt(split[1]), split[2], split[3]));//将从文件src/Inventory.txt中数据写入到容器inventorylist容器
            itemToIndex.put(split[0], inventoryList.size() - 1);
        }
        inventoryReader.close();//关闭文件
        System.out.println("Inventory.txt文件读取完成");

        List<List<String>> transactionsList = new ArrayList<>();
        for(int i = 0; i < 4; i++){
            transactionsList.add(new ArrayList<>());
        }

        BufferedReader transactionsReader = new BufferedReader(new FileReader("Transactions.txt"));//从文件src/Transactions.txt中读取数据
        while((s = transactionsReader.readLine()) != null){
            switch(s.charAt(0)){
                case 'O':
                    transactionsList.get(0).add(s);
                    break;
                case 'R':
                    transactionsList.get(1).add(s);
                    break;
                case 'A':
                    transactionsList.get(2).add(s);
                    break;
                case 'D':
                    transactionsList.get(3).add(s);
                    break;
                default:
                    break;
            }
        }
        transactionsReader.close();
        System.out.println("Transaction文件读取完成");



        
        for(String temp : transactionsList.get(2)){
            String[] split = temp.split("\t");
            inventoryList.add(new Inventory(split[1], 0, split[2], split[3]));
            itemToIndex.put(split[1], inventoryList.size() - 1);
        }

        for(String temp : transactionsList.get(1)){
            String[] split = temp.split("\t");
            inventoryList.get(itemToIndex.get(split[1])).Quantity += Integer.parseInt(split[2]);
        }

        Collections.sort(transactionsList.get(0), new SortByString());

        Map<String, Pair<String, Integer>> shippingList = new HashMap<>();

        BufferedWriter errorWriter = new BufferedWriter(new FileWriter("src/Errors.txt"));
        for(String temp : transactionsList.get(0)){
            String[] split = temp.split("\t");
            if(Integer.parseInt(split[2]) <= inventoryList.get(itemToIndex.get(split[1])).Quantity){
                inventoryList.get(itemToIndex.get(split[1])).Quantity -= Integer.parseInt(split[2]);
                if(shippingList.containsKey(split[3]) && shippingList.get(split[3]).getKey() == split[1]){
                    shippingList.put(split[3], new Pair<String, Integer>(split[1],
                            Integer.parseInt(split[2]) + shippingList.get(split[3]).getValue()));
                }else{
                    shippingList.put(split[3], new Pair<String, Integer>(split[1], Integer.parseInt(split[2])));
                }
            }else{
                errorWriter.write(split[3] + "\t" + split[1] + "\t" + split[2]);
                errorWriter.newLine();
            }
        }
        for(String temp : transactionsList.get(3)){
            String[] split = temp.split("\t");
            if(inventoryList.get(itemToIndex.get(split[1])).Quantity > 0){
                errorWriter.write("0" + "\t" + itemToIndex.get(split[1]) + "\t"
                        + inventoryList.get(itemToIndex.get(split[1])).Quantity);
                errorWriter.newLine();
            }
            inventoryList.remove((int)itemToIndex.get(split[1]));
        }
        errorWriter.close();
        System.out.println("Errors文件写入完成");

        BufferedWriter shippingWriter = new BufferedWriter(new FileWriter("src/Shipping.txt"));
        for (Map.Entry<String, Pair<String, Integer>> entry : shippingList.entrySet()) {
            shippingWriter.write(entry.getKey() + "\t"
                    + entry.getValue().getKey() + "\t" + entry.getValue().getValue());
            shippingWriter.newLine();
        }
        shippingWriter.close();
        System.out.println("shipping文件写入完成");
        //将新仓库内的物品写入
        BufferedWriter newInventoryWriter = new BufferedWriter(new FileWriter("src/NewInventory.txt"));
        for(Inventory inventory : inventoryList){
            newInventoryWriter.write(inventory.itemQuantity + "\t" + inventory.Quantity
                    + "\t" + inventory.supplier + "\t" + inventory.description);
            newInventoryWriter.newLine();
        }
        newInventoryWriter.close();
        System.out.println("newInventory文件写入完成");
        System.out.println("请查看相应文件，了解进出货情况");
    }

    
}

class SortByString implements Comparator {//比较函数类，并返回1或-1
    public int compare(Object o1, Object o2) {
        String[] s1 = ((String)o1).split("\t");
        String[] s2 = ((String)o2).split("\t");
        return Integer.parseInt(s1[2]) > Integer.parseInt(s2[2]) ? 1 : -1;
    }
}