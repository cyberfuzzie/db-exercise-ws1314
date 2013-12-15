<?php

class Chart {
    
    public static $parteiConfig = array(
        'aggregate' => array(
            'CDU/CSU' => array('CDU', 'CSU')
        ),
        'color' => array(
            'default' => '#0000AA',
            'CDU/CSU' => '#000000',
            'SPD' => '#FF0000',
            'GRÜNE' => '#00AA00',
            'DIE LINKE' => '#AA0000'
        )
    );
    
    static function getPieData($data, $config) {
        $aggregatedData = array();
        foreach($data as $row) {
            foreach($config['aggregate'] as $union => $members) {
                if (in_array($row[1], $members)) {
                    if (array_key_exists($union, $aggregatedData)) {
                        $aggregatedData[$union] += $row[0];
                    } else {
                        $aggregatedData[$union] = $row[0];
                    }
                    continue 2;
                }
            }
            // no union found
            $aggregatedData[$row[1]] = $row[0];
        }
        
        $pieData = array();
        foreach($aggregatedData as $key => $value) {
            $elem = array();
            $elem['title'] = $key;
            $elem['value'] = $value;
            if (array_key_exists($key, $config['color'])) {
                $elem['color'] = $config['color'][$key];
            } else {
                $elem['color'] = $config['color']['default'];
            }
            $pieData[] = $elem;
        }
        
        return $pieData;
    }
}