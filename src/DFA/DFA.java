package src.dfa;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;

public class DFA {
    private Map<Node, Collection<Transformation>> transformTable;

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
        this.end = end;
    }

    /**
     * 给一次输入，运行一次DFA
     * @param input 输入
     * @throws NoSuchTransformationException 转换失败异常
     * @return 是否到达结束状态
     */
    public boolean run(Input input) throws NoSuchTransformationException {
        for (Transformation transformation :
                this.transformTable.get(current)) {
            if (transformation.getTransform().transform(current, input)) {
                //满足转换条件
                current = transformation.getDestination();
                transformation.getAction().doIt(current);
                return current == end;
            }
        }
        throw new NoSuchTransformationException(current, input, "There is no matching transformation.");
    }

    /**
     * 添加状态转换
     *
     * @param from 转换源节点
     * @param to 转换目标节点
     * @param transformable 判断是否可转换的函数
     */
    public void addTransform(Node from, Node to, Transformable transformable) throws InvalidTransformationException {
        this.addTransform(from, to, transformable, element -> {
        });
    }

    /**
     * 添加状态转化
     *
     * @param from 转换源节点
     * @param to 转换目标节点
     * @param transformable 判断是否可转换的函数
     * @param doable 转换完成后在目标节点执行的操作
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
