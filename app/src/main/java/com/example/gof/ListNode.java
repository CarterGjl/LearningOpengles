package com.example.gof;

class ListNode {

    int val;
    ListNode next;

    public ListNode(int val) {
        this.val = val;
    }

    public boolean hasCycle(ListNode head) {
        ListNode runner = head.next;
        ListNode walker = head;
        while (runner != null && walker != null) {
            walker = walker.next;
            ListNode next = runner.next;
            if (next != null) {
                runner = next.next;
            } else {
                runner = null;
            }
            if (walker == runner) {
                return true;
            }
        }
        return false;
    }

    public ListNode mergeTwoLists(ListNode l1, ListNode l2) {
        // 类似归并排序中的合并过程
        ListNode dummyHead = new ListNode(0);
        ListNode cur = dummyHead;
        while (l1 != null && l2 != null) {
            if (l1.val < l2.val) {
                cur.next = l1;
                cur = cur.next;
                l1 = l1.next;
            } else {
                cur.next = l2;
                cur = cur.next;
                l2 = l2.next;
            }
        }
        // 任一为空，直接连接另一条链表
        if (l1 == null) {
            cur.next = l2;
        } else {
            cur.next = l1;
        }
        return dummyHead.next;
    }
}
