package dfa;

import model.Parser;
import model.Token;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;

public class DFA {
    private Map<dfa.Node, Collection<Transformation>> transformTable;

    /**
     * 开始节点
     */
    private Node start;

    /**
     * 结束节点
     * 为null时永不結束
     */
    private Node end;

    /**
     * 当前节点
     */
    private Node current;

    public DFA(Node start, Node end) {
        transformTable = new Hashtable<>();
        this.start = start;
        this.current = start;
        this.end = end;
    }

    /**
     * 重置当前节点为开始节点
     */
    public void reset() {
        current = start;
    }

    /**
     * 给一次输入，运行一次DFA
     *
     * @param input 输入
     * @return 是否到达结束状态
     * @throws NoSuchTransformationException 转换失败异常
     */
    public boolean run(Object input) throws NoSuchTransformationException {
        for (Transformation transformation :
                this.transformTable.get(current)) {
            if (transformation.getTransform().transform(current, input)) {
                //满足转换条件
                current = transformation.getDestination();
                transformation.getAction().doIt(current, input);
//                System.out.println("current : "+current.getName());
//                System.out.println("input: " + input.toString());
//                System.out.println("-----------------------------");
                return current == end;
            }
        }
//        System.out.println("current : "+current.getName());
//        System.out.println("input: " + input.toString());
//        System.out.println("-----------------------------");
        throw new NoSuchTransformationException(current, input, "There is no matching transformation.");
    }

    /**
     * 添加状态转换
     *
     * @param from          转换源节点
     * @param to            转换目标节点
     * @param transformable 判断是否可转换的函数
     */
    public void addTransform(Node from, Node to, Transformable transformable) throws InvalidTransformationException {
        this.addTransform(from, to, transformable, (destNode, input) -> {
        });
    }

    /**
     * 添加状态转化
     *
     * @param from          转换源节点
     * @param to            转换目标节点
     * @param transformable 判断是否可转换的函数
     * @param doable        转换完成后在目标节点执行的操作
     */
    public void addTransform(Node from, Node to, Transformable transformable, Doable doable) throws InvalidTransformationException {
        if (!this.transformTable.containsKey(from)) {
            this.transformTable.put(from, new HashSet<>());
        }
        for (Transformation transformation :
                this.transformTable.get(from)) {
            if (transformation.getTransform() == transformable) {
                throw new InvalidTransformationException("Transformation denied. Transformation with exactly same transformable already exists.");
            }
        }
        this.transformTable.get(from).add(new Transformation(to, transformable, doable));
    }
}
