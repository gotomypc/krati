/*
 * Copyright (c) 2011 LinkedIn, Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package krati.store.bus.protocol;

import krati.store.avro.protocol.StoreKeys;
import krati.store.bus.AvroStoreBus;

/**
 * AvroStoreBusResponder
 * 
 * @author jwu
 * @since 10/04, 2011
 */
public class AvroStoreBusResponder<K> extends StoreBusResponder {
    
    public AvroStoreBusResponder(AvroStoreBus<K> storeBus) {
        super(new AvroStoreBusProtocolHandler<K>(storeBus));
        setProperty(StoreKeys.KRATI_STORE_VALUE_SCHEMA,
                    storeBus.getStore().getSchema().toString());
    }
}
