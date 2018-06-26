package com.sap.cisp.xhna.data.executor;

import java.util.List;
import java.util.Map;
import com.sap.cisp.xhna.data.db.HANAService;

public class TestMediaTestExecutor extends AbstractTaskExecutor {
//    BlockingQueue queue = new LinkedBlockingQueue();

    @Override
    public void start(Map<String, Object> ctx) throws Exception {
        System.out.println("start " + task.getParam());

    }

    @Override
    public void complete(Map<String, Object> ctx) throws Exception {
        System.out.println("complete " + task.getParam());

    }

    @Override
    public List<String> execute(Map<String, Object> ctx)
            throws InterruptedException, Exception {

        if (ctx.get("count") != null) {
            return null;
        } else {
            ctx.put("count", true);
            System.out.println("execute " + task.getParam());
            int count = 0;
            while (count < 2) {
                count++;
                HANAService.listAllMediaInfos();
            }
            // try {
            // // Thread.sleep(15000L);
            // queue.take();
            // } catch (InterruptedException e) {
            // throw e;
            // }
            return null;
            // return task.getParam()+" Done!";
        }
    }

    @Override
    public void save(Map<String, Object> ctx, List<String> data)
            throws Exception {
        System.out.println("save " + task.getParam() + " Data " + data);
    }

}
